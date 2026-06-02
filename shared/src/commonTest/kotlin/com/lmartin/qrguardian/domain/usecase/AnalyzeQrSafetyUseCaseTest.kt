package com.lmartin.qrguardian.domain.usecase

import com.lmartin.qrguardian.domain.analyzer.QrSecurityAnalyzer
import com.lmartin.qrguardian.domain.metadata.DownloadFileType
import com.lmartin.qrguardian.domain.metadata.UrlMetadataRepository
import com.lmartin.qrguardian.domain.metadata.UrlMetadataResult
import com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.QrSecurityResult
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.reputation.ThreatCategory
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnalyzeQrSafetyUseCaseTest {
    @Test
    fun `non url skips metadata and remote reputation`() = runBlocking {
        val localAnalyzer = RecordingQrSecurityAnalyzer(
            result = QrSecurityResult(
                originalText = "hello world",
                normalizedText = "hello world",
                contentType = QrContentType.PlainText,
                securityLevel = SecurityLevel.Unknown,
                title = SecurityLevel.Unknown.title(),
                description = SecurityLevel.Unknown.description(),
                reasons = listOf("Plain text is not evaluated as a URL."),
                canOpen = true
            )
        )
        val metadataRepository = RecordingUrlMetadataRepository(
            result = unavailableMetadataResult(),
            gate = CompletableDeferred()
        )
        val reputationRepository = RecordingUrlReputationRepository(
            result = UrlReputationResult(
                status = UrlReputationStatus.Clean,
                provider = "Dummy",
                categories = emptyList(),
                reasons = listOf("No threats were reported by the external reputation service.")
            ),
            gate = CompletableDeferred()
        )
        val useCase = AnalyzeQrSafetyUseCase(localAnalyzer, metadataRepository, reputationRepository)

        val result = useCase("hello world")

        assertEquals(SecurityLevel.Unknown, result.overallLevel)
        assertTrue(result.canOpen)
        assertEquals(ScanStatus.Completed, result.localScan.status)
        assertEquals(ScanStatus.NotApplicable, result.remoteReputation.status)
        assertEquals(0, metadataRepository.callCount)
        assertEquals(0, reputationRepository.callCount)
        assertEquals(1, localAnalyzer.callCount)
    }

    @Test
    fun `url analysis runs metadata and reputation in parallel`() = runBlocking {
        val metadataGate = CompletableDeferred<Unit>()
        val reputationGate = CompletableDeferred<Unit>()
        val localAnalyzer = RecordingQrSecurityAnalyzer(
            result = QrSecurityResult(
                originalText = "example.com/download",
                normalizedText = "example.com/download",
                contentType = QrContentType.Url,
                securityLevel = SecurityLevel.Safe,
                title = SecurityLevel.Safe.title(),
                description = SecurityLevel.Safe.description(),
                reasons = emptyList(),
                canOpen = true
            )
        )
        val metadataRepository = RecordingUrlMetadataRepository(
            result = UrlMetadataResult(
                status = UrlMetadataStatus.Available,
                finalUrl = "https://example.com/download",
                contentType = "application/vnd.android.package-archive",
                contentDisposition = """attachment; filename="app.apk"""",
                contentLength = 2048L,
                fileName = "app.apk",
                fileExtension = "apk",
                fileType = DownloadFileType.AndroidApp,
                isLikelyDownload = true,
                reasons = emptyList()
            ),
            gate = metadataGate
        )
        val reputationRepository = RecordingUrlReputationRepository(
            result = UrlReputationResult(
                status = UrlReputationStatus.Clean,
                provider = "Dummy",
                categories = emptyList(),
                reasons = listOf("No threats were reported by the external reputation service.")
            ),
            gate = reputationGate
        )
        val useCase = AnalyzeQrSafetyUseCase(localAnalyzer, metadataRepository, reputationRepository)

        var result: com.lmartin.qrguardian.domain.model.QrAnalysisResult? = null
        val job = launch {
            result = useCase("example.com/download")
        }

        withTimeout(1_000) {
            while (!metadataRepository.started || !reputationRepository.started) {
                delay(1)
            }
        }

        assertEquals(1, metadataRepository.callCount)
        assertEquals(1, reputationRepository.callCount)
        assertEquals(1, localAnalyzer.callCount)

        metadataGate.complete(Unit)
        reputationGate.complete(Unit)
        job.join()

        val analysisResult = result!!
        assertEquals(SecurityLevel.Dangerous, analysisResult.overallLevel)
        assertFalse(analysisResult.canOpen)
        assertEquals(SecurityLevel.Dangerous, analysisResult.localScan.level)
        assertEquals(ScanStatus.Completed, analysisResult.localScan.status)
        assertTrue(analysisResult.localScan.metadata.any { it.label == "File type" && it.value == "Android app" })
        assertEquals(SecurityLevel.Safe, analysisResult.remoteReputation.level)
        assertEquals(ScanStatus.Completed, analysisResult.remoteReputation.status)
    }

    @Test
    fun `remote malicious makes overall result dangerous`() = runBlocking {
        val localAnalyzer = RecordingQrSecurityAnalyzer(
            result = QrSecurityResult(
                originalText = "https://example.com",
                normalizedText = "https://example.com",
                contentType = QrContentType.Url,
                securityLevel = SecurityLevel.Safe,
                title = SecurityLevel.Safe.title(),
                description = SecurityLevel.Safe.description(),
                reasons = emptyList(),
                canOpen = true
            )
        )
        val metadataRepository = RecordingUrlMetadataRepository(
            result = UrlMetadataResult(
                status = UrlMetadataStatus.Available,
                finalUrl = "https://example.com",
                contentType = "text/html",
                contentDisposition = null,
                contentLength = null,
                fileName = null,
                fileExtension = null,
                fileType = DownloadFileType.Unknown,
                isLikelyDownload = false,
                reasons = emptyList()
            ),
            gate = CompletableDeferred<Unit>().apply { complete(Unit) }
        )
        val reputationRepository = RecordingUrlReputationRepository(
            result = UrlReputationResult(
                status = UrlReputationStatus.Malicious,
                provider = "Dummy",
                categories = listOf(ThreatCategory.Phishing),
                reasons = listOf("Threat reported by the external reputation service.")
            ),
            gate = CompletableDeferred<Unit>().apply { complete(Unit) }
        )
        val useCase = AnalyzeQrSafetyUseCase(localAnalyzer, metadataRepository, reputationRepository)

        val result = useCase("https://example.com")

        assertEquals(SecurityLevel.Dangerous, result.overallLevel)
        assertFalse(result.canOpen)
        assertEquals(ScanStatus.Completed, result.localScan.status)
        assertEquals(ScanStatus.Completed, result.remoteReputation.status)
        assertTrue(result.remoteReputation.metadata.any { it.label == "Categories" && it.value == "Phishing" })
    }

    private fun unavailableMetadataResult(): UrlMetadataResult {
        return UrlMetadataResult(
            status = UrlMetadataStatus.Unavailable,
            finalUrl = null,
            contentType = null,
            contentDisposition = null,
            contentLength = null,
            fileName = null,
            fileExtension = null,
            fileType = DownloadFileType.Unknown,
            isLikelyDownload = false,
            reasons = listOf("Destination metadata could not be checked.")
        )
    }

    private class RecordingQrSecurityAnalyzer(
        private val result: QrSecurityResult
    ) : QrSecurityAnalyzer {
        var callCount: Int = 0
            private set

        override fun analyze(rawText: String): QrSecurityResult {
            callCount += 1
            return result
        }
    }

    private class RecordingUrlMetadataRepository(
        private val result: UrlMetadataResult,
        private val gate: CompletableDeferred<Unit>
    ) : UrlMetadataRepository {
        var callCount: Int = 0
            private set
        var started: Boolean = false
            private set

        override suspend fun fetchMetadata(url: String): UrlMetadataResult {
            callCount += 1
            started = true
            gate.await()
            return result
        }
    }

    private class RecordingUrlReputationRepository(
        private val result: UrlReputationResult,
        private val gate: CompletableDeferred<Unit>
    ) : UrlReputationRepository {
        var callCount: Int = 0
            private set
        var started: Boolean = false
            private set

        override suspend fun checkUrl(url: String): UrlReputationResult {
            callCount += 1
            started = true
            gate.await()
            return result
        }
    }
}

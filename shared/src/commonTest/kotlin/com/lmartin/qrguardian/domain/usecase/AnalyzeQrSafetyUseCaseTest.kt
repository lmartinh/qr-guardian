package com.lmartin.qrguardian.domain.usecase

import com.lmartin.qrguardian.data.metadata.KtorUrlMetadataRepository
import com.lmartin.qrguardian.data.reputation.NoOpUrlReputationRepository
import com.lmartin.qrguardian.domain.analyzer.LocalScanAnalyzer
import com.lmartin.qrguardian.domain.classifier.QrContentClassifier
import com.lmartin.qrguardian.domain.metadata.DownloadFileType
import com.lmartin.qrguardian.domain.metadata.UrlMetadataRepository
import com.lmartin.qrguardian.domain.metadata.UrlMetadataResult
import com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.reputation.ThreatCategory
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
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
        val localAnalyzer =
            RecordingLocalScanAnalyzer(
                result =
                scanSection(
                    level = SecurityLevel.Unknown,
                    reasons = listOf("Plain text is not evaluated as a URL."),
                ),
            )
        val metadataRepository =
            RecordingUrlMetadataRepository(
                result = unavailableMetadataResult(),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val reputationRepository =
            RecordingUrlReputationRepository(
                result =
                UrlReputationResult(
                    status = UrlReputationStatus.Clean,
                    provider = "Dummy",
                    categories = emptyList(),
                    reasons = listOf("No threats were reported by the external reputation service."),
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val useCase =
            AnalyzeQrSafetyUseCase(
                localScanAnalyzer = localAnalyzer,
                urlMetadataRepository = metadataRepository,
                urlReputationRepository = reputationRepository,
            )

        val result = useCase("hello world")

        assertEquals(SecurityLevel.Unknown, result.overallLevel)
        assertFalse(result.canOpen)
        assertEquals(null, result.openableUrl)
        assertEquals(QrContentType.PlainText, result.contentType)
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
        val localAnalyzer =
            RecordingLocalScanAnalyzer(
                result =
                scanSection(
                    level = SecurityLevel.Safe,
                    reasons = emptyList(),
                ),
            )
        val metadataRepository =
            RecordingUrlMetadataRepository(
                result =
                UrlMetadataResult(
                    status = UrlMetadataStatus.Available,
                    finalUrl = "https://example.com/download",
                    contentType = "application/pdf",
                    contentDisposition = """inline; filename="report.pdf"""",
                    contentLength = 2048L,
                    fileName = "report.pdf",
                    fileExtension = "pdf",
                    fileType = DownloadFileType.Pdf,
                    isLikelyDownload = false,
                    reasons = emptyList(),
                    resourceKind = com.lmartin.qrguardian.domain.metadata.UrlResourceKind.Document,
                ),
                gate = metadataGate,
            )
        val reputationRepository =
            RecordingUrlReputationRepository(
                result =
                UrlReputationResult(
                    status = UrlReputationStatus.Clean,
                    provider = "Dummy",
                    categories = emptyList(),
                    reasons = listOf("No threats were reported by the external reputation service."),
                ),
                gate = reputationGate,
            )
        val useCase =
            AnalyzeQrSafetyUseCase(
                localScanAnalyzer = localAnalyzer,
                urlMetadataRepository = metadataRepository,
                urlReputationRepository = reputationRepository,
            )

        var result: QrAnalysisResult? = null
        val job =
            launch {
                result = useCase("https://example.com/download")
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
        assertEquals(SecurityLevel.Safe, analysisResult.overallLevel)
        assertTrue(analysisResult.canOpen)
        assertEquals("https://example.com/download", analysisResult.openableUrl)
        assertEquals(ScanStatus.Completed, analysisResult.localScan.status)
        assertTrue(analysisResult.localScan.metadata.any { it.label == "Host" && it.value == "example.com" })
        assertTrue(analysisResult.localScan.metadata.any { it.label == "Connection" && it.value == "HTTPS" })
        assertTrue(analysisResult.localScan.metadata.any { it.label == "Content" && it.value == "PDF document" })
        assertTrue(analysisResult.localScan.metadata.any { it.label == "File type" && it.value == "PDF" })
        assertFalse(analysisResult.localScan.metadata.any { it.label == "Download" })
        assertEquals(ScanStatus.Completed, analysisResult.remoteReputation.status)
    }

    @Test
    fun `pdf url still resolves file metadata when head is not allowed`() = runBlocking {
        val useCase =
            AnalyzeQrSafetyUseCase(
                urlMetadataRepository =
                KtorUrlMetadataRepository(
                    httpClient =
                    HttpClient(
                        MockEngine {
                            respond(
                                content = "",
                                status = HttpStatusCode.MethodNotAllowed,
                                headers = headersOf(),
                            )
                        },
                    ),
                ),
                urlReputationRepository = NoOpUrlReputationRepository(),
            )

        val result = useCase("https://grupodanigarcia.com/wp-content/uploads/CARTAS/LENA/MADRID/ES/LENAMADRID_BRUNCH.pdf")

        assertEquals(QrContentType.Url, result.contentType)
        assertEquals(ScanStatus.Completed, result.localScan.status)
        assertTrue(result.localScan.metadata.any { it.label == "Content" && it.value == "PDF document" })
        assertTrue(result.localScan.metadata.any { it.label == "File name" && it.value == "LENAMADRID_BRUNCH.pdf" })
        assertTrue(result.localScan.metadata.any { it.label == "File type" && it.value == "PDF" })
        assertFalse(result.localScan.metadata.any { it.label == "Path" })
    }

    @Test
    fun `bare domain opens with https scheme`() = runBlocking {
        val localAnalyzer =
            RecordingLocalScanAnalyzer(
                result =
                scanSection(
                    level = SecurityLevel.Suspicious,
                    reasons = listOf("The URL does not use HTTPS."),
                ),
            )
        val metadataRepository =
            RecordingUrlMetadataRepository(
                result = unavailableMetadataResult(),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val reputationRepository =
            RecordingUrlReputationRepository(
                result =
                UrlReputationResult(
                    status = UrlReputationStatus.Clean,
                    provider = "Dummy",
                    categories = emptyList(),
                    reasons = emptyList(),
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val useCase =
            AnalyzeQrSafetyUseCase(
                localScanAnalyzer = localAnalyzer,
                urlMetadataRepository = metadataRepository,
                urlReputationRepository = reputationRepository,
            )

        val result = useCase("example.com")

        assertEquals(QrContentType.Url, result.contentType)
        assertEquals(SecurityLevel.Suspicious, result.overallLevel)
        assertTrue(result.canOpen)
        assertEquals("https://example.com", result.openableUrl)
    }

    @Test
    fun `http url keeps http scheme for opening`() = runBlocking {
        val localAnalyzer =
            RecordingLocalScanAnalyzer(
                result =
                scanSection(
                    level = SecurityLevel.Suspicious,
                    reasons = listOf("The URL does not use HTTPS."),
                ),
            )
        val metadataRepository =
            RecordingUrlMetadataRepository(
                result = unavailableMetadataResult(),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val reputationRepository =
            RecordingUrlReputationRepository(
                result =
                UrlReputationResult(
                    status = UrlReputationStatus.Clean,
                    provider = "Dummy",
                    categories = emptyList(),
                    reasons = emptyList(),
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val useCase =
            AnalyzeQrSafetyUseCase(
                localScanAnalyzer = localAnalyzer,
                urlMetadataRepository = metadataRepository,
                urlReputationRepository = reputationRepository,
            )

        val result = useCase("http://example.com")

        assertEquals(QrContentType.Url, result.contentType)
        assertEquals(SecurityLevel.Suspicious, result.overallLevel)
        assertTrue(result.canOpen)
        assertEquals("http://example.com", result.openableUrl)
    }

    @Test
    fun `simple url exposes host scheme and web page content without fake file details`() = runBlocking {
        val localAnalyzer =
            RecordingLocalScanAnalyzer(
                result =
                scanSection(
                    level = SecurityLevel.Safe,
                    reasons = emptyList(),
                ),
            )
        val metadataRepository =
            RecordingUrlMetadataRepository(
                result =
                UrlMetadataResult(
                    status = UrlMetadataStatus.Available,
                    finalUrl = null,
                    contentType = "text/html",
                    contentDisposition = null,
                    contentLength = null,
                    fileName = null,
                    fileExtension = null,
                    fileType = DownloadFileType.Unknown,
                    isLikelyDownload = false,
                    reasons = emptyList(),
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val reputationRepository =
            RecordingUrlReputationRepository(
                result =
                UrlReputationResult(
                    status = UrlReputationStatus.Clean,
                    provider = "Dummy",
                    categories = emptyList(),
                    reasons = emptyList(),
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val useCase =
            AnalyzeQrSafetyUseCase(
                localScanAnalyzer = localAnalyzer,
                urlMetadataRepository = metadataRepository,
                urlReputationRepository = reputationRepository,
            )

        val result = useCase("https://example.com")

        assertEquals(QrContentType.Url, result.contentType)
        assertEquals(SecurityLevel.Safe, result.overallLevel)
        assertTrue(result.localScan.metadata.any { it.label == "Host" && it.value == "example.com" })
        assertTrue(result.localScan.metadata.any { it.label == "Connection" && it.value == "HTTPS" })
        assertTrue(result.localScan.metadata.any { it.label == "Content" && it.value == "Web page" })
        assertFalse(result.localScan.metadata.any { it.label == "File name" })
        assertFalse(result.localScan.metadata.any { it.label == "File type" })
        assertFalse(result.localScan.metadata.any { it.label == "Download" })
    }

    @Test
    fun `download path with html content keeps local scan as a page`() = runBlocking {
        val localAnalyzer =
            RecordingLocalScanAnalyzer(
                result =
                scanSection(
                    level = SecurityLevel.Safe,
                    reasons = emptyList(),
                ),
            )
        val metadataRepository =
            RecordingUrlMetadataRepository(
                result =
                UrlMetadataResult(
                    status = UrlMetadataStatus.Available,
                    finalUrl = null,
                    contentType = "text/html",
                    contentDisposition = null,
                    contentLength = null,
                    fileName = null,
                    fileExtension = null,
                    fileType = DownloadFileType.Unknown,
                    isLikelyDownload = false,
                    reasons = emptyList(),
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val reputationRepository =
            RecordingUrlReputationRepository(
                result =
                UrlReputationResult(
                    status = UrlReputationStatus.Clean,
                    provider = "Dummy",
                    categories = emptyList(),
                    reasons = emptyList(),
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val useCase =
            AnalyzeQrSafetyUseCase(
                localScanAnalyzer = localAnalyzer,
                urlMetadataRepository = metadataRepository,
                urlReputationRepository = reputationRepository,
            )

        val result = useCase("https://example.com/download")

        assertEquals(SecurityLevel.Safe, result.overallLevel)
        assertTrue(result.localScan.metadata.any { it.label == "Host" && it.value == "example.com" })
        assertTrue(result.localScan.metadata.any { it.label == "Connection" && it.value == "HTTPS" })
        assertTrue(result.localScan.metadata.any { it.label == "Path" && it.value == "/download" })
        assertTrue(result.localScan.metadata.any { it.label == "Content" && it.value == "Web page" })
        assertFalse(result.localScan.metadata.any { it.label == "File name" })
        assertFalse(result.localScan.metadata.any { it.label == "File type" })
        assertFalse(result.localScan.metadata.any { it.label == "Download" })
    }

    @Test
    fun `attachment metadata keeps result suspicious`() = runBlocking {
        val localAnalyzer =
            RecordingLocalScanAnalyzer(
                result =
                scanSection(
                    level = SecurityLevel.Safe,
                    reasons = emptyList(),
                ),
            )
        val metadataRepository =
            RecordingUrlMetadataRepository(
                result =
                UrlMetadataResult(
                    status = UrlMetadataStatus.Available,
                    finalUrl = "https://example.com/file",
                    contentType = "application/octet-stream",
                    contentDisposition = """attachment; filename="payload.bin"""",
                    contentLength = 512L,
                    fileName = "payload.bin",
                    fileExtension = "bin",
                    fileType = DownloadFileType.Unknown,
                    isLikelyDownload = true,
                    reasons = emptyList(),
                    resourceKind = com.lmartin.qrguardian.domain.metadata.UrlResourceKind.UnknownBinary,
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val reputationRepository =
            RecordingUrlReputationRepository(
                result =
                UrlReputationResult(
                    status = UrlReputationStatus.Clean,
                    provider = "Dummy",
                    categories = emptyList(),
                    reasons = emptyList(),
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val useCase =
            AnalyzeQrSafetyUseCase(
                localScanAnalyzer = localAnalyzer,
                urlMetadataRepository = metadataRepository,
                urlReputationRepository = reputationRepository,
            )

        val result = useCase("https://example.com/file")

        assertEquals(SecurityLevel.Suspicious, result.overallLevel)
        assertTrue(result.canOpen)
        assertEquals("https://example.com/file", result.openableUrl)
        assertTrue(result.localScan.metadata.any { it.label == "Content" && it.value == "Unknown binary file" })
        assertTrue(result.localScan.metadata.any { it.label == "Download" && it.value == "Server suggests a file download" })
        assertTrue(result.localScan.metadata.any { it.label == "File name" && it.value == "payload.bin" })
        assertTrue(result.localScan.metadata.any { it.label == "File type" && it.value == "File" })
    }

    @Test
    fun `executable metadata makes result dangerous`() = runBlocking {
        val localAnalyzer =
            RecordingLocalScanAnalyzer(
                result =
                scanSection(
                    level = SecurityLevel.Safe,
                    reasons = emptyList(),
                ),
            )
        val metadataRepository =
            RecordingUrlMetadataRepository(
                result =
                UrlMetadataResult(
                    status = UrlMetadataStatus.Available,
                    finalUrl = "https://example.com/installer.exe",
                    contentType = "application/octet-stream",
                    contentDisposition = """attachment; filename="installer.exe"""",
                    contentLength = 1024L,
                    fileName = "installer.exe",
                    fileExtension = "exe",
                    fileType = DownloadFileType.WindowsExecutable,
                    isLikelyDownload = true,
                    reasons = emptyList(),
                    resourceKind = com.lmartin.qrguardian.domain.metadata.UrlResourceKind.InstallerOrExecutable,
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val reputationRepository =
            RecordingUrlReputationRepository(
                result =
                UrlReputationResult(
                    status = UrlReputationStatus.Clean,
                    provider = "Dummy",
                    categories = emptyList(),
                    reasons = emptyList(),
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val useCase =
            AnalyzeQrSafetyUseCase(
                localScanAnalyzer = localAnalyzer,
                urlMetadataRepository = metadataRepository,
                urlReputationRepository = reputationRepository,
            )

        val result = useCase("https://example.com/installer.exe")

        assertEquals(SecurityLevel.Dangerous, result.overallLevel)
        assertFalse(result.canOpen)
        assertEquals(null, result.openableUrl)
        assertTrue(result.localScan.metadata.any { it.label == "Content" && it.value == "Windows executable" })
        assertTrue(result.localScan.metadata.any { it.label == "Download" && it.value == "Server suggests a file download" })
        assertTrue(result.localScan.metadata.any { it.label == "File name" && it.value == "installer.exe" })
        assertTrue(result.localScan.metadata.any { it.label == "File type" && it.value == "Windows executable" })
    }

    @Test
    fun `remote malicious makes overall result dangerous`() = runBlocking {
        val localAnalyzer =
            RecordingLocalScanAnalyzer(
                result =
                scanSection(
                    level = SecurityLevel.Safe,
                    reasons = emptyList(),
                ),
            )
        val metadataRepository =
            RecordingUrlMetadataRepository(
                result =
                UrlMetadataResult(
                    status = UrlMetadataStatus.Available,
                    finalUrl = "https://example.com",
                    contentType = "text/html",
                    contentDisposition = null,
                    contentLength = null,
                    fileName = null,
                    fileExtension = null,
                    fileType = DownloadFileType.Unknown,
                    isLikelyDownload = false,
                    reasons = emptyList(),
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val reputationRepository =
            RecordingUrlReputationRepository(
                result =
                UrlReputationResult(
                    status = UrlReputationStatus.Malicious,
                    provider = "Dummy",
                    categories = listOf(ThreatCategory.Phishing),
                    reasons = listOf("Threat reported by the external reputation service."),
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val useCase =
            AnalyzeQrSafetyUseCase(
                localScanAnalyzer = localAnalyzer,
                urlMetadataRepository = metadataRepository,
                urlReputationRepository = reputationRepository,
            )

        val result = useCase("https://example.com")

        assertEquals(SecurityLevel.Dangerous, result.overallLevel)
        assertFalse(result.canOpen)
        assertEquals(null, result.openableUrl)
        assertEquals(ScanStatus.Completed, result.localScan.status)
        assertEquals(ScanStatus.Completed, result.remoteReputation.status)
        assertTrue(result.remoteReputation.metadata.any { it.label == "Categories" && it.value == "Phishing" })
    }

    @Test
    fun `dangerous schemes are blocked before url work starts`() = runBlocking {
        val localAnalyzer =
            RecordingLocalScanAnalyzer(
                result =
                scanSection(
                    level = SecurityLevel.Safe,
                    reasons = emptyList(),
                ),
            )
        val metadataRepository =
            RecordingUrlMetadataRepository(
                result = unavailableMetadataResult(),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val reputationRepository =
            RecordingUrlReputationRepository(
                result =
                UrlReputationResult(
                    status = UrlReputationStatus.Clean,
                    provider = "Dummy",
                    categories = emptyList(),
                    reasons = emptyList(),
                ),
                gate = CompletableDeferred<Unit>().apply { complete(Unit) },
            )
        val useCase =
            AnalyzeQrSafetyUseCase(
                localScanAnalyzer = localAnalyzer,
                urlMetadataRepository = metadataRepository,
                urlReputationRepository = reputationRepository,
            )

        val result = useCase("javascript:alert(1)")

        assertEquals(QrContentType.Unknown, result.contentType)
        assertEquals(SecurityLevel.Dangerous, result.overallLevel)
        assertFalse(result.canOpen)
        assertEquals(null, result.openableUrl)
        assertEquals(0, localAnalyzer.callCount)
        assertEquals(0, metadataRepository.callCount)
        assertEquals(0, reputationRepository.callCount)
        assertEquals(ScanStatus.NotApplicable, result.remoteReputation.status)
    }

    private fun scanSection(
        level: SecurityLevel,
        reasons: List<String>,
        metadata: List<com.lmartin.qrguardian.domain.model.ScanMetadataItem> = emptyList(),
    ): ScanSectionResult = ScanSectionResult(
        name = "Local Scan",
        level = level,
        status = ScanStatus.Completed,
        title = level.title(),
        description = level.description(),
        reasons = reasons,
        metadata = metadata,
    )

    private fun unavailableMetadataResult(): UrlMetadataResult = UrlMetadataResult(
        status = UrlMetadataStatus.Unavailable,
        finalUrl = null,
        contentType = null,
        contentDisposition = null,
        contentLength = null,
        fileName = null,
        fileExtension = null,
        fileType = DownloadFileType.Unknown,
        isLikelyDownload = false,
        reasons = emptyList(),
    )

    private class RecordingLocalScanAnalyzer(
        private val result: ScanSectionResult,
    ) : LocalScanAnalyzer {
        var callCount: Int = 0
            private set

        override fun analyze(
            rawText: String,
            normalizedText: String,
            contentType: QrContentType,
        ): ScanSectionResult {
            callCount += 1
            return result
        }
    }

    private class RecordingUrlMetadataRepository(
        private val result: UrlMetadataResult,
        private val gate: CompletableDeferred<Unit>,
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
        private val gate: CompletableDeferred<Unit>,
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

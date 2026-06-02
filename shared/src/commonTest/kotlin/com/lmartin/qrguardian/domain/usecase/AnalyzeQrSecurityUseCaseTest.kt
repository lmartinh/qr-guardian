package com.lmartin.qrguardian.domain.usecase

import com.lmartin.qrguardian.domain.analyzer.DefaultQrSecurityAnalyzer
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.reputation.ThreatCategory
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnalyzeQrSecurityUseCaseTest {
    @Test
    fun `safe url keeps local safe result with not configured remote status`() = runBlocking {
        val repository = RecordingUrlReputationRepository(
            result = UrlReputationResult(
                status = UrlReputationStatus.NotConfigured,
                provider = "None",
                categories = emptyList(),
                reasons = listOf("Remote reputation checks are not configured.")
            )
        )
        val useCase = AnalyzeQrSecurityUseCase(DefaultQrSecurityAnalyzer(), repository)

        val result = useCase("https://example.com")

        assertEquals(QrContentType.Url, result.contentType)
        assertEquals(SecurityLevel.Safe, result.securityLevel)
        assertTrue(result.canOpen)
        assertEquals(UrlReputationStatus.NotConfigured, result.remoteReputationStatus)
        assertEquals(1, repository.callCount)
    }

    @Test
    fun `suspicious url keeps local suspicious result with not configured remote status`() = runBlocking {
        val repository = RecordingUrlReputationRepository(
            result = UrlReputationResult(
                status = UrlReputationStatus.NotConfigured,
                provider = "None",
                categories = emptyList(),
                reasons = listOf("Remote reputation checks are not configured.")
            )
        )
        val useCase = AnalyzeQrSecurityUseCase(DefaultQrSecurityAnalyzer(), repository)

        val result = useCase("http://example.com")

        assertEquals(SecurityLevel.Suspicious, result.securityLevel)
        assertTrue(result.canOpen)
        assertEquals(UrlReputationStatus.NotConfigured, result.remoteReputationStatus)
        assertEquals(1, repository.callCount)
    }

    @Test
    fun `dangerous url keeps local dangerous result with not configured remote status`() = runBlocking {
        val repository = RecordingUrlReputationRepository(
            result = UrlReputationResult(
                status = UrlReputationStatus.NotConfigured,
                provider = "None",
                categories = emptyList(),
                reasons = listOf("Remote reputation checks are not configured.")
            )
        )
        val useCase = AnalyzeQrSecurityUseCase(DefaultQrSecurityAnalyzer(), repository)

        val result = useCase("https://example.com/file.apk")

        assertEquals(SecurityLevel.Dangerous, result.securityLevel)
        assertFalse(result.canOpen)
        assertEquals(UrlReputationStatus.NotConfigured, result.remoteReputationStatus)
        assertEquals(1, repository.callCount)
    }

    @Test
    fun `plain text does not call remote reputation checks`() = runBlocking {
        val repository = RecordingUrlReputationRepository(
            result = UrlReputationResult(
                status = UrlReputationStatus.Clean,
                provider = "Dummy",
                categories = emptyList(),
                reasons = listOf("No threats were reported by the external reputation service.")
            )
        )
        val useCase = AnalyzeQrSecurityUseCase(DefaultQrSecurityAnalyzer(), repository)

        val result = useCase("hello world")

        assertEquals(SecurityLevel.Unknown, result.securityLevel)
        assertEquals(UrlReputationStatus.NotConfigured, result.remoteReputationStatus)
        assertEquals(0, repository.callCount)
    }

    @Test
    fun `use case does not duplicate repeated reasons`() = runBlocking {
        val repository = RecordingUrlReputationRepository(
            result = UrlReputationResult(
                status = UrlReputationStatus.NotConfigured,
                provider = "None",
                categories = emptyList(),
                reasons = listOf(
                    "Remote reputation checks are not configured.",
                    "Remote reputation checks are not configured."
                )
            )
        )
        val useCase = AnalyzeQrSecurityUseCase(DefaultQrSecurityAnalyzer(), repository)

        val result = useCase("https://example.com")

        assertEquals(1, result.reasons.count { it == "Remote reputation checks are not configured." })
    }

    @Test
    fun `remote malicious makes final result dangerous`() = runBlocking {
        val repository = RecordingUrlReputationRepository(
            result = UrlReputationResult(
                status = UrlReputationStatus.Malicious,
                provider = "Dummy",
                categories = listOf(ThreatCategory.Phishing),
                reasons = listOf("Threat reported by the external reputation service.")
            )
        )
        val useCase = AnalyzeQrSecurityUseCase(DefaultQrSecurityAnalyzer(), repository)

        val result = useCase("https://example.com")

        assertEquals(SecurityLevel.Dangerous, result.securityLevel)
        assertFalse(result.canOpen)
    }

    @Test
    fun `remote suspicious upgrades safe result to suspicious`() = runBlocking {
        val repository = RecordingUrlReputationRepository(
            result = UrlReputationResult(
                status = UrlReputationStatus.Suspicious,
                provider = "Dummy",
                categories = listOf(ThreatCategory.Unknown),
                reasons = listOf("Potential concern reported by the external reputation service.")
            )
        )
        val useCase = AnalyzeQrSecurityUseCase(DefaultQrSecurityAnalyzer(), repository)

        val result = useCase("https://example.com")

        assertEquals(SecurityLevel.Suspicious, result.securityLevel)
        assertTrue(result.canOpen)
    }

    @Test
    fun `remote clean does not make suspicious url safe`() = runBlocking {
        val repository = RecordingUrlReputationRepository(
            result = UrlReputationResult(
                status = UrlReputationStatus.Clean,
                provider = "Dummy",
                categories = emptyList(),
                reasons = listOf("No threats were reported by the external reputation service.")
            )
        )
        val useCase = AnalyzeQrSecurityUseCase(DefaultQrSecurityAnalyzer(), repository)

        val result = useCase("http://example.com")

        assertEquals(SecurityLevel.Suspicious, result.securityLevel)
        assertTrue(result.reasons.contains("No threats were reported by the external reputation service."))
    }

    @Test
    fun `remote error keeps local result`() = runBlocking {
        val repository = RecordingUrlReputationRepository(
            result = UrlReputationResult(
                status = UrlReputationStatus.Error,
                provider = "Dummy",
                categories = emptyList(),
                reasons = listOf("Remote reputation check is currently unavailable.")
            )
        )
        val useCase = AnalyzeQrSecurityUseCase(DefaultQrSecurityAnalyzer(), repository)

        val result = useCase("http://example.com")

        assertEquals(SecurityLevel.Suspicious, result.securityLevel)
        assertTrue(result.canOpen)
    }

    private class RecordingUrlReputationRepository(
        private val result: UrlReputationResult
    ) : UrlReputationRepository {
        var callCount: Int = 0
            private set

        override suspend fun checkUrl(url: String): UrlReputationResult {
            callCount += 1
            return result
        }
    }
}

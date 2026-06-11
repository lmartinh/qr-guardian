package com.lmartin.qrguardian.domain.analyzer

import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.usecase.AnalyzeQrSafetyUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultLocalScanAnalyzerTest {
    private val analyzer = DefaultLocalScanAnalyzer()

    @Test
    fun `safe https url stays safe`() {
        val result =
            analyzer.analyze(
                rawText = "https://example.com",
                normalizedText = "https://example.com",
                contentType = QrContentType.Url,
            )

        assertEquals(SecurityLevel.Safe, result.level)
        assertEquals("Looks safe", result.title)
    }

    @Test
    fun `http url is suspicious`() {
        val result =
            analyzer.analyze(
                rawText = "http://example.com",
                normalizedText = "http://example.com",
                contentType = QrContentType.Url,
            )

        assertEquals(SecurityLevel.Suspicious, result.level)
    }

    @Test
    fun `dangerous file url is dangerous`() {
        val result =
            analyzer.analyze(
                rawText = "https://example.com/file.apk",
                normalizedText = "https://example.com/file.apk",
                contentType = QrContentType.Url,
            )

        assertEquals(SecurityLevel.Dangerous, result.level)
    }

    @Test
    fun `plain text stays unknown`() {
        val result =
            analyzer.analyze(
                rawText = "hello world",
                normalizedText = "hello world",
                contentType = QrContentType.PlainText,
            )

        assertEquals(SecurityLevel.Unknown, result.level)
    }

    @Test
    fun `blank normalized text is ignored as unknown`() {
        val result =
            analyzer.analyze(
                rawText = "   ",
                normalizedText = "   ",
                contentType = QrContentType.PlainText,
            )

        assertEquals(SecurityLevel.Unknown, result.level)
        assertEquals(listOf("The scanned text is empty after normalization."), result.reasons)
    }

    @Test
    fun `control characters make the local normalization suspicious`() {
        val result =
            analyzer.analyze(
                rawText = "hello\u0001world",
                normalizedText = "hello\u0001world",
                contentType = QrContentType.PlainText,
            )

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("suspicious control characters") })
    }

    @Test
    fun `very long text triggers the local size threshold`() {
        val longText = buildString {
            append("https://example.com/")
            repeat(4100) {
                append('a')
            }
        }

        val result =
            analyzer.analyze(
                rawText = longText,
                normalizedText = longText,
                contentType = QrContentType.Url,
            )

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("longer than the local safety threshold") })
    }

    @Test
    fun `null and delete characters are reported during normalization`() {
        val result =
            analyzer.analyze(
                rawText = "hello\u0000world\u007F",
                normalizedText = "helloworld",
                contentType = QrContentType.PlainText,
            )

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("Null characters were removed") })
        assertTrue(result.reasons.any { it.contains("suspicious control characters") })
    }

    @Test
    fun `phone content is routed through the sensitive action analyzer`() {
        val result =
            analyzer.analyze(
                rawText = "tel:+34600000000",
                normalizedText = "tel:+34600000000",
                contentType = QrContentType.Phone,
            )

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("Telephone links can start a sensitive action") })
    }

    @Test
    fun `wifi content is routed through the wifi analyzer`() {
        val result =
            analyzer.analyze(
                rawText = "WIFI:T:WPA;P:secret;;",
                normalizedText = "WIFI:T:WPA;P:secret;;",
                contentType = QrContentType.Wifi,
            )

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("missing the network name") })
    }

    @Test
    fun `unknown content type stays unknown when text is otherwise clean`() {
        val result =
            analyzer.analyze(
                rawText = "payload",
                normalizedText = "payload",
                contentType = QrContentType.Unknown,
            )

        assertEquals(SecurityLevel.Unknown, result.level)
        assertTrue(result.reasons.any { it.contains("could not be fully evaluated locally") })
    }

    @Test
    fun `dangerous scheme is blocked as dangerous`() {
        val useCase =
            AnalyzeQrSafetyUseCase(
                urlMetadataRepository = NoOpUrlMetadataRepository(),
                urlReputationRepository = NoOpUrlReputationRepository(),
            )

        val result =
            kotlinx.coroutines.runBlocking {
                useCase("javascript:alert(1)")
            }

        assertEquals(QrContentType.Unknown, result.contentType)
        assertEquals(SecurityLevel.Dangerous, result.overallLevel)
        assertEquals(false, result.canOpen)
        assertEquals("Local Scan", result.localScan.name)
    }

    private class NoOpUrlMetadataRepository : com.lmartin.qrguardian.domain.metadata.UrlMetadataRepository {
        override suspend fun fetchMetadata(url: String) = com.lmartin.qrguardian.domain.metadata.UrlMetadataResult(
            status = com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus.NotApplicable,
            finalUrl = null,
            contentType = null,
            contentDisposition = null,
            contentLength = null,
            fileName = null,
            fileExtension = null,
            fileType = com.lmartin.qrguardian.domain.metadata.DownloadFileType.Unknown,
            isLikelyDownload = false,
            reasons = emptyList(),
        )
    }

    private class NoOpUrlReputationRepository : com.lmartin.qrguardian.domain.reputation.UrlReputationRepository {
        override suspend fun checkUrl(url: String) = com.lmartin.qrguardian.domain.reputation.UrlReputationResult(
            status = com.lmartin.qrguardian.domain.reputation.UrlReputationStatus.NotConfigured,
            provider = "None",
            categories = emptyList(),
            reasons = emptyList(),
        )
    }
}

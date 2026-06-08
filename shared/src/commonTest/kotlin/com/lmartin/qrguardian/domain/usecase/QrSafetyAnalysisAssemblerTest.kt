package com.lmartin.qrguardian.domain.usecase

import com.lmartin.qrguardian.domain.metadata.DownloadFileType
import com.lmartin.qrguardian.domain.metadata.UrlMetadataResult
import com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QrSafetyAnalysisAssemblerTest {
    @Test
    fun `simple url shows host scheme and web page content only`() {
        val result =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "text/html",
                    fileName = null,
                    fileExtension = null,
                    fileType = DownloadFileType.Unknown,
                    isLikelyDownload = false,
                ),
                sourceUrl = "https://example.com",
            )

        assertEquals(SecurityLevel.Safe, result.level)
        assertTrue(result.metadata.any { it.label == "Host" && it.value == "example.com" })
        assertTrue(result.metadata.any { it.label == "Connection" && it.value == "HTTPS" })
        assertTrue(result.metadata.any { it.label == "Content" && it.value == "Web page" })
        assertFalse(result.metadata.any { it.label == "File name" })
        assertFalse(result.metadata.any { it.label == "File type" })
        assertFalse(result.metadata.any { it.label == "Download" })
    }

    @Test
    fun `html download path does not show file or download rows`() {
        val result =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "text/html",
                    fileName = null,
                    fileExtension = null,
                    fileType = DownloadFileType.Unknown,
                    isLikelyDownload = false,
                ),
                sourceUrl = "https://example.com/download",
            )

        assertEquals(SecurityLevel.Safe, result.level)
        assertTrue(result.metadata.any { it.label == "Path" && it.value == "/download" })
        assertTrue(result.metadata.any { it.label == "Content" && it.value == "Web page" })
        assertFalse(result.metadata.any { it.label == "File name" })
        assertFalse(result.metadata.any { it.label == "File type" })
        assertFalse(result.metadata.any { it.label == "Download" })
    }

    @Test
    fun `pdf url exposes file metadata and download signal`() {
        val result =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "application/pdf",
                    fileName = "report.pdf",
                    fileExtension = "pdf",
                    fileType = DownloadFileType.Pdf,
                    isLikelyDownload = true,
                ),
                sourceUrl = "https://example.com/report.pdf",
            )

        assertEquals(SecurityLevel.Safe, result.level)
        assertTrue(result.metadata.any { it.label == "Content" && it.value == "PDF document" })
        assertTrue(result.metadata.any { it.label == "File name" && it.value == "report.pdf" })
        assertTrue(result.metadata.any { it.label == "File type" && it.value == "PDF" })
        assertTrue(result.metadata.any { it.label == "Download" && it.value == "Downloadable file" })
        assertFalse(result.metadata.any { it.label == "Resolved destination" })
    }

    @Test
    fun `apk url is marked dangerous and exposes download metadata`() {
        val result =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "application/vnd.android.package-archive",
                    fileName = "app.apk",
                    fileExtension = "apk",
                    fileType = DownloadFileType.AndroidApp,
                    isLikelyDownload = true,
                ),
                sourceUrl = "https://example.com/app.apk",
            )

        assertEquals(SecurityLevel.Dangerous, result.level)
        assertTrue(result.metadata.any { it.label == "Content" && it.value == "Android app" })
        assertTrue(result.metadata.any { it.label == "File name" && it.value == "app.apk" })
        assertTrue(result.metadata.any { it.label == "File type" && it.value == "Android app" })
        assertTrue(result.metadata.any { it.label == "Download" && it.value == "Downloadable file" })
    }

    private fun scanSection(level: SecurityLevel): ScanSectionResult = ScanSectionResult(
        name = "Local Scan",
        level = level,
        status = ScanStatus.Completed,
        title = level.title(),
        description = level.description(),
        reasons = emptyList(),
    )

    private fun metadata(
        contentType: String?,
        fileName: String?,
        fileExtension: String?,
        fileType: DownloadFileType,
        isLikelyDownload: Boolean,
    ): UrlMetadataResult = UrlMetadataResult(
        status = UrlMetadataStatus.Available,
        finalUrl = null,
        contentType = contentType,
        contentDisposition = null,
        contentLength = null,
        fileName = fileName,
        fileExtension = fileExtension,
        fileType = fileType,
        isLikelyDownload = isLikelyDownload,
        reasons = emptyList(),
    )
}

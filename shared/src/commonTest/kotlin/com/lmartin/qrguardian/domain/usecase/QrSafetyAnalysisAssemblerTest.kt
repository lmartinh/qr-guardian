package com.lmartin.qrguardian.domain.usecase

import com.lmartin.qrguardian.domain.metadata.DownloadFileType
import com.lmartin.qrguardian.domain.metadata.UrlMetadataResult
import com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus
import com.lmartin.qrguardian.domain.metadata.UrlResourceKind
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
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
    fun `pdf url exposes file metadata without download signal`() {
        val result =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "application/pdf",
                    fileName = "report.pdf",
                    fileExtension = "pdf",
                    fileType = DownloadFileType.Pdf,
                    isLikelyDownload = false,
                    resourceKind = com.lmartin.qrguardian.domain.metadata.UrlResourceKind.Document,
                ),
                sourceUrl = "https://example.com/report.pdf",
            )

        assertEquals(SecurityLevel.Safe, result.level)
        assertTrue(result.metadata.any { it.label == "Content" && it.value == "PDF document" })
        assertTrue(result.metadata.any { it.label == "File name" && it.value == "report.pdf" })
        assertTrue(result.metadata.any { it.label == "File type" && it.value == "PDF" })
        assertTrue(result.metadata.none { it.label == "Download" })
        assertFalse(result.metadata.any { it.label == "Resolved destination" })
    }

    @Test
    fun `archive url is suspicious and exposes download metadata`() {
        val result =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "application/zip",
                    fileName = "archive.zip",
                    fileExtension = "zip",
                    fileType = DownloadFileType.Archive,
                    isLikelyDownload = true,
                    resourceKind = com.lmartin.qrguardian.domain.metadata.UrlResourceKind.Archive,
                ),
                sourceUrl = "https://example.com/archive.zip",
            )

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.metadata.any { it.label == "Content" && it.value == "Archive" })
        assertTrue(result.metadata.any { it.label == "File name" && it.value == "archive.zip" })
        assertTrue(result.metadata.any { it.label == "File type" && it.value == "Archive" })
        assertTrue(result.metadata.any { it.label == "Download" && it.value == "Downloadable file" })
        assertTrue(result.reasons.any { it.contains("Compressed files can hide other files inside.") })
    }

    @Test
    fun `apk url is dangerous and exposes download metadata`() {
        val result =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "application/vnd.android.package-archive",
                    fileName = "app.apk",
                    fileExtension = "apk",
                    fileType = DownloadFileType.AndroidApp,
                    isLikelyDownload = true,
                    resourceKind = com.lmartin.qrguardian.domain.metadata.UrlResourceKind.InstallerOrExecutable,
                ),
                sourceUrl = "https://example.com/app.apk",
            )

        assertEquals(SecurityLevel.Dangerous, result.level)
        assertTrue(result.metadata.any { it.label == "Content" && it.value == "Android app" })
        assertTrue(result.metadata.any { it.label == "File name" && it.value == "app.apk" })
        assertTrue(result.metadata.any { it.label == "File type" && it.value == "Android app" })
        assertTrue(result.metadata.any { it.label == "Download" && it.value == "Downloadable file" })
        assertTrue(result.reasons.any { it.contains("run code or install software on your device") })
    }

    @Test
    fun `remote section maps provider states and preserves metadata`() {
        val clean =
            QrSafetyAnalysisAssembler.buildRemoteSection(
                UrlReputationResult(
                    status = com.lmartin.qrguardian.domain.reputation.UrlReputationStatus.Clean,
                    provider = "Provider A",
                    categories = emptyList(),
                    reasons = listOf("Clean"),
                ),
            )
        val suspicious =
            QrSafetyAnalysisAssembler.buildRemoteSection(
                UrlReputationResult(
                    status = com.lmartin.qrguardian.domain.reputation.UrlReputationStatus.Suspicious,
                    provider = "Provider B",
                    categories = listOf(com.lmartin.qrguardian.domain.reputation.ThreatCategory.Phishing),
                    reasons = listOf("Suspicious"),
                ),
            )
        val malicious =
            QrSafetyAnalysisAssembler.buildRemoteSection(
                UrlReputationResult(
                    status = com.lmartin.qrguardian.domain.reputation.UrlReputationStatus.Malicious,
                    provider = "Provider C",
                    categories = listOf(com.lmartin.qrguardian.domain.reputation.ThreatCategory.Malware),
                    reasons = listOf("Malicious"),
                ),
            )
        val unknown =
            QrSafetyAnalysisAssembler.buildRemoteSection(
                UrlReputationResult(
                    status = com.lmartin.qrguardian.domain.reputation.UrlReputationStatus.Unknown,
                    provider = "Provider D",
                    categories = listOf(com.lmartin.qrguardian.domain.reputation.ThreatCategory.Unknown),
                    reasons = listOf("Unknown"),
                ),
            )
        val notConfigured =
            QrSafetyAnalysisAssembler.buildRemoteSection(
                UrlReputationResult(
                    status = com.lmartin.qrguardian.domain.reputation.UrlReputationStatus.NotConfigured,
                    provider = "Provider E",
                    categories = emptyList(),
                    reasons = emptyList(),
                ),
            )
        val error =
            QrSafetyAnalysisAssembler.buildRemoteSection(
                UrlReputationResult(
                    status = com.lmartin.qrguardian.domain.reputation.UrlReputationStatus.Error,
                    provider = "Provider F",
                    categories = emptyList(),
                    reasons = emptyList(),
                ),
            )

        assertEquals(ScanStatus.Completed, clean.status)
        assertEquals(ScanStatus.Completed, suspicious.status)
        assertEquals(ScanStatus.Completed, malicious.status)
        assertEquals(ScanStatus.Completed, unknown.status)
        assertEquals(ScanStatus.NotConfigured, notConfigured.status)
        assertEquals(ScanStatus.Unavailable, error.status)
        assertTrue(suspicious.metadata.any { it.label == "Provider" && it.value == "Provider B" })
        assertTrue(suspicious.metadata.any { it.label == "Categories" && it.value == "Phishing" })
        assertTrue(malicious.metadata.any { it.label == "Categories" && it.value == "Malware" })
        assertTrue(unknown.metadata.any { it.label == "Categories" && it.value == "Unknown" })
        assertTrue(notConfigured.metadata.isEmpty())
        assertTrue(error.metadata.isEmpty())
    }

    @Test
    fun `local section maps resource metadata into labels and reasons`() {
        val result =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult =
                UrlMetadataResult(
                    status = UrlMetadataStatus.Available,
                    finalUrl = "https://cdn.example.com/file",
                    contentType = "application/octet-stream",
                    contentDisposition = null,
                    contentLength = null,
                    fileName = "payload.bin",
                    fileExtension = "bin",
                    fileType = DownloadFileType.Unknown,
                    isLikelyDownload = true,
                    reasons = emptyList(),
                    resourceKind = UrlResourceKind.UnknownBinary,
                ),
                sourceUrl = "https://example.com/file",
            )

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.metadata.any { it.label == "Content" && it.value == "Unknown binary file" })
        assertTrue(result.metadata.any { it.label == "File name" && it.value == "payload.bin" })
        assertTrue(result.metadata.any { it.label == "File type" && it.value == "File" })
        assertTrue(result.metadata.any { it.label == "Download" && it.value == "Downloadable file" })
        assertTrue(result.reasons.any { it.contains("resolves to a different URL") })
        assertTrue(result.reasons.any { it.contains("generic binary download") })
    }

    @Test
    fun `content labels cover web pages documents and media`() {
        val webPage =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "text/html",
                    fileName = null,
                    fileExtension = null,
                    fileType = DownloadFileType.Unknown,
                    isLikelyDownload = false,
                    resourceKind = UrlResourceKind.WebPage,
                ),
                sourceUrl = "https://example.com",
            )
        val image =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "image/png",
                    fileName = "image.png",
                    fileExtension = "png",
                    fileType = DownloadFileType.Image,
                    isLikelyDownload = false,
                    resourceKind = UrlResourceKind.Image,
                ),
                sourceUrl = "https://example.com/image.png",
            )
        val audio =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "audio/mpeg",
                    fileName = "song.mp3",
                    fileExtension = "mp3",
                    fileType = DownloadFileType.Audio,
                    isLikelyDownload = false,
                    resourceKind = UrlResourceKind.Media,
                ),
                sourceUrl = "https://example.com/song.mp3",
            )
        val video =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "video/mp4",
                    fileName = "clip.mp4",
                    fileExtension = "mp4",
                    fileType = DownloadFileType.Video,
                    isLikelyDownload = false,
                    resourceKind = UrlResourceKind.Media,
                ),
                sourceUrl = "https://example.com/clip.mp4",
            )
        val document =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    fileName = "report.docx",
                    fileExtension = "docx",
                    fileType = DownloadFileType.Document,
                    isLikelyDownload = true,
                    resourceKind = UrlResourceKind.Document,
                ),
                sourceUrl = "https://example.com/report.docx",
            )
        val spreadsheet =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = null,
                    fileName = "sheet.xlsx",
                    fileExtension = "xlsx",
                    fileType = DownloadFileType.Spreadsheet,
                    isLikelyDownload = true,
                    resourceKind = UrlResourceKind.Document,
                ),
                sourceUrl = "https://example.com/sheet.xlsx",
            )
        val presentation =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = null,
                    fileName = "slides.pptx",
                    fileExtension = "pptx",
                    fileType = DownloadFileType.Presentation,
                    isLikelyDownload = true,
                    resourceKind = UrlResourceKind.Document,
                ),
                sourceUrl = "https://example.com/slides.pptx",
            )

        assertTrue(webPage.metadata.any { it.label == "Content" && it.value == "Web page" })
        assertTrue(image.metadata.any { it.label == "Content" && it.value == "Image" })
        assertTrue(audio.metadata.any { it.label == "Content" && it.value == "Audio" })
        assertTrue(video.metadata.any { it.label == "Content" && it.value == "Video" })
        assertTrue(document.metadata.any { it.label == "Content" && it.value == "Document" })
        assertTrue(spreadsheet.metadata.any { it.label == "Content" && it.value == "Spreadsheet" })
        assertTrue(presentation.metadata.any { it.label == "Content" && it.value == "Presentation" })
    }

    @Test
    fun `content labels cover archive executable and fallback branches`() {
        val xhtml =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "application/xhtml+xml",
                    fileName = null,
                    fileExtension = null,
                    fileType = DownloadFileType.Unknown,
                    isLikelyDownload = false,
                    resourceKind = UrlResourceKind.WebPage,
                ),
                sourceUrl = "https://example.com/page",
            )
        val sevenZip =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "application/x-7z-compressed",
                    fileName = "archive.7z",
                    fileExtension = "7z",
                    fileType = DownloadFileType.Archive,
                    isLikelyDownload = true,
                    resourceKind = UrlResourceKind.Archive,
                ),
                sourceUrl = "https://example.com/archive.7z",
            )
        val tar =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "application/x-tar",
                    fileName = "archive.tar",
                    fileExtension = "tar",
                    fileType = DownloadFileType.Archive,
                    isLikelyDownload = true,
                    resourceKind = UrlResourceKind.Archive,
                ),
                sourceUrl = "https://example.com/archive.tar",
            )
        val diskImage =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = null,
                    fileName = "disk.dmg",
                    fileExtension = "dmg",
                    fileType = DownloadFileType.AppleDiskImage,
                    isLikelyDownload = true,
                    resourceKind = UrlResourceKind.InstallerOrExecutable,
                ),
                sourceUrl = "https://example.com/disk.dmg",
            )
        val genericFile =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = "text/plain",
                    fileName = null,
                    fileExtension = null,
                    fileType = DownloadFileType.Unknown,
                    isLikelyDownload = false,
                    resourceKind = UrlResourceKind.Unknown,
                ),
                sourceUrl = "https://example.com/file.txt",
            )
        val blankUnknown =
            QrSafetyAnalysisAssembler.buildLocalSection(
                baseLocalScan = scanSection(SecurityLevel.Safe),
                metadataResult = metadata(
                    contentType = null,
                    fileName = null,
                    fileExtension = null,
                    fileType = DownloadFileType.Unknown,
                    isLikelyDownload = false,
                    resourceKind = UrlResourceKind.Unknown,
                ),
                sourceUrl = "https://example.com/",
            )

        assertTrue(xhtml.metadata.any { it.label == "Content" && it.value == "Web page" })
        assertTrue(sevenZip.metadata.any { it.label == "Content" && it.value == "Archive" })
        assertTrue(tar.metadata.any { it.label == "Content" && it.value == "Archive" })
        assertTrue(diskImage.metadata.any { it.label == "Content" && it.value == "Apple disk image" })
        assertTrue(genericFile.metadata.any { it.label == "Content" && it.value == "File" })
        assertFalse(blankUnknown.metadata.any { it.label == "Content" })
    }

    @Test
    fun `combine levels preserves the most severe signal`() {
        assertEquals(SecurityLevel.Dangerous, QrSafetyAnalysisAssembler.combineLevels(SecurityLevel.Safe, SecurityLevel.Dangerous))
        assertEquals(SecurityLevel.Suspicious, QrSafetyAnalysisAssembler.combineLevels(SecurityLevel.Safe, SecurityLevel.Suspicious))
        assertEquals(SecurityLevel.Safe, QrSafetyAnalysisAssembler.combineLevels(SecurityLevel.Safe, null))
        assertEquals(SecurityLevel.Unknown, QrSafetyAnalysisAssembler.combineLevels(SecurityLevel.Unknown, null))
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
        resourceKind: com.lmartin.qrguardian.domain.metadata.UrlResourceKind = com.lmartin.qrguardian.domain.metadata.UrlResourceKind.Unknown,
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
        resourceKind = resourceKind,
    )
}

package com.lmartin.qrguardian.domain.metadata

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UrlMetadataInferenceTest {
    @Test
    fun `normalize url for request trims input and adds https when missing`() {
        assertEquals("", normalizeUrlForRequest("   "))
        assertEquals("https://example.com/path", normalizeUrlForRequest("  example.com/path  "))
        assertEquals("http://example.com/path", normalizeUrlForRequest("http://example.com/path"))
    }

    @Test
    fun `content disposition filename extraction prefers filename star and plain filename`() {
        assertEquals(
            "report-from-server.pdf",
            extractFilenameFromContentDisposition("""attachment; filename*=UTF-8''report-from-server.pdf"""),
        )
        assertEquals(
            "installer.apk",
            extractFilenameFromContentDisposition("""attachment; filename="installer.apk""""),
        )
        assertNull(extractFilenameFromContentDisposition(null))
    }

    @Test
    fun `file name inference respects web page extensions and attachments`() {
        assertNull(inferFileNameFromPath("/about.html", null, null))
        assertEquals("menu.pdf", inferFileNameFromPath("/menu.pdf", null, null))
        assertEquals("report.pdf", inferFileNameFromPath("/ignored", "application/pdf", """attachment; filename="report.pdf""""))
    }

    @Test
    fun `file extension inference handles blank names and dotted names`() {
        assertNull(inferFileExtensionFromName(null))
        assertNull(inferFileExtensionFromName("report"))
        assertEquals("pdf", inferFileExtensionFromName("report.pdf"))
        assertEquals("gz", inferFileExtensionFromName("archive.tar.gz"))
    }

    @Test
    fun `file type inference maps common download types`() {
        val cases =
            listOf(
                Triple("report.pdf", null, DownloadFileType.Pdf),
                Triple("photo.jpg", null, DownloadFileType.Image),
                Triple("song.mp3", null, DownloadFileType.Audio),
                Triple("video.mp4", null, DownloadFileType.Video),
                Triple("archive.zip", null, DownloadFileType.Archive),
                Triple("document.docx", null, DownloadFileType.Document),
                Triple("sheet.xlsx", null, DownloadFileType.Spreadsheet),
                Triple("slides.pptx", null, DownloadFileType.Presentation),
                Triple("app.apk", null, DownloadFileType.AndroidApp),
                Triple("disk.dmg", null, DownloadFileType.AppleDiskImage),
                Triple("tool.exe", null, DownloadFileType.WindowsExecutable),
                Triple("script.sh", null, DownloadFileType.Script),
                Triple("file.bin", "application/octet-stream", DownloadFileType.Unknown),
            )

        cases.forEach { (fileName, contentType, expected) ->
            assertEquals(expected, inferFileTypeFromExtension(fileName, null, contentType), fileName)
        }
    }

    @Test
    fun `resource kind and download signal follow the inferred file metadata`() {
        assertEquals(
            UrlResourceKind.WebPage,
            inferUrlResourceKind(
                contentType = "text/html",
                contentDisposition = null,
                fileName = null,
                fileExtension = null,
                fileType = DownloadFileType.Unknown,
            ),
        )
        assertEquals(
            UrlResourceKind.Document,
            inferUrlResourceKind(
                contentType = "application/pdf",
                contentDisposition = """attachment; filename="report.pdf"""",
                fileName = "report.pdf",
                fileExtension = "pdf",
                fileType = DownloadFileType.Pdf,
            ),
        )
        assertEquals(
            UrlResourceKind.UnknownBinary,
            inferUrlResourceKind(
                contentType = "application/octet-stream",
                contentDisposition = null,
                fileName = null,
                fileExtension = null,
                fileType = DownloadFileType.Unknown,
            ),
        )
        assertEquals(
            UrlResourceKind.OtherFile,
            inferUrlResourceKind(
                contentType = null,
                contentDisposition = null,
                fileName = "payload.bin",
                fileExtension = "bin",
                fileType = DownloadFileType.Unknown,
            ),
        )

        assertTrue(
            shouldShowPath(
                path = "/download/payload.bin",
                fileName = null,
                resourceKind = UrlResourceKind.WebPage,
            ),
        )
        assertFalse(
            shouldShowPath(
                path = "/download/payload.bin",
                fileName = "payload.bin",
                resourceKind = UrlResourceKind.OtherFile,
            ),
        )
        assertFalse(
            shouldShowPath(
                path = "/",
                fileName = null,
                resourceKind = UrlResourceKind.WebPage,
            ),
        )
    }

    @Test
    fun `file size formatting and display names stay stable`() {
        assertEquals("Unknown", formatFileSize(-1))
        assertEquals("512 B", formatFileSize(512))
        assertEquals("1.5 KB", formatFileSize(1536))
        assertEquals("2 MB", formatFileSize(2L * 1024 * 1024))

        val displayNames =
            mapOf(
                DownloadFileType.Pdf to "PDF",
                DownloadFileType.Document to "Document",
                DownloadFileType.Spreadsheet to "Spreadsheet",
                DownloadFileType.Presentation to "Presentation",
                DownloadFileType.Image to "Image",
                DownloadFileType.Audio to "Audio",
                DownloadFileType.Video to "Video",
                DownloadFileType.Archive to "Archive",
                DownloadFileType.AndroidApp to "Android app",
                DownloadFileType.AppleDiskImage to "Apple disk image",
                DownloadFileType.WindowsExecutable to "Windows executable",
                DownloadFileType.Script to "Script",
                DownloadFileType.Unknown to "Unknown",
            )

        displayNames.forEach { (type, expected) ->
            assertEquals(expected, type.displayName(), type.name)
        }
    }
}

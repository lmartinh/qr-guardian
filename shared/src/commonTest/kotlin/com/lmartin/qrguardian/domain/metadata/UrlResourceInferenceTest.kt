package com.lmartin.qrguardian.domain.metadata

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UrlResourceInferenceTest {
    @Test
    fun `web page content and attachments infer the expected resource kind`() {
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
    }

    @Test
    fun `file type mapping covers document media and executable branches`() {
        assertEquals(UrlResourceKind.Document, inferUrlResourceKind(null, null, "report.docx", "docx", DownloadFileType.Document))
        assertEquals(UrlResourceKind.Image, inferUrlResourceKind(null, null, "photo.jpg", "jpg", DownloadFileType.Image))
        assertEquals(UrlResourceKind.Media, inferUrlResourceKind(null, null, "song.mp3", "mp3", DownloadFileType.Audio))
        assertEquals(UrlResourceKind.Media, inferUrlResourceKind(null, null, "video.mp4", "mp4", DownloadFileType.Video))
        assertEquals(UrlResourceKind.Archive, inferUrlResourceKind(null, null, "archive.zip", "zip", DownloadFileType.Archive))
        assertEquals(UrlResourceKind.InstallerOrExecutable, inferUrlResourceKind(null, null, "app.apk", "apk", DownloadFileType.AndroidApp))
        assertEquals(UrlResourceKind.InstallerOrExecutable, inferUrlResourceKind(null, null, "disk.dmg", "dmg", DownloadFileType.AppleDiskImage))
        assertEquals(UrlResourceKind.InstallerOrExecutable, inferUrlResourceKind(null, null, "tool.exe", "exe", DownloadFileType.WindowsExecutable))
        assertEquals(UrlResourceKind.InstallerOrExecutable, inferUrlResourceKind(null, null, "script.sh", "sh", DownloadFileType.Script))
        assertEquals(UrlResourceKind.WebPage, inferUrlResourceKind(null, null, null, null, DownloadFileType.Unknown))
        assertEquals(UrlResourceKind.OtherFile, inferUrlResourceKind(null, null, "payload.bin", "bin", DownloadFileType.Unknown))
    }

    @Test
    fun `path visibility respects web pages and file metadata`() {
        assertFalse(shouldShowPath("/", null, UrlResourceKind.WebPage))
        assertFalse(shouldShowPath("", null, UrlResourceKind.WebPage))
        assertFalse(shouldShowPath("/download/file.bin", "file.bin", UrlResourceKind.OtherFile))
        assertTrue(shouldShowPath("/download", null, UrlResourceKind.WebPage))
        assertTrue(shouldShowPath("/download", "file.bin", UrlResourceKind.WebPage))
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
}

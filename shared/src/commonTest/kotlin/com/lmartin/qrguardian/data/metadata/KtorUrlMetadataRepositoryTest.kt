package com.lmartin.qrguardian.data.metadata

import com.lmartin.qrguardian.domain.metadata.DownloadFileType
import com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KtorUrlMetadataRepositoryTest {
    @Test
    fun `head response metadata is parsed`() = runBlocking {
        val repository =
            KtorUrlMetadataRepository(
                httpClient =
                httpClient(
                    status = HttpStatusCode.OK,
                    headers =
                    listOf(
                        HttpHeaders.ContentType to listOf("application/vnd.android.package-archive; charset=utf-8"),
                        HttpHeaders.ContentDisposition to listOf("""attachment; filename="installer.apk""""),
                        HttpHeaders.ContentLength to listOf("2048"),
                    ),
                ),
            )

        val result = repository.fetchMetadata("example.com/download")

        assertEquals(UrlMetadataStatus.Available, result.status)
        assertEquals(null, result.finalUrl)
        assertEquals("application/vnd.android.package-archive", result.contentType)
        assertEquals("""attachment; filename="installer.apk"""", result.contentDisposition)
        assertEquals(2048L, result.contentLength)
        assertEquals("installer.apk", result.fileName)
        assertEquals("apk", result.fileExtension)
        assertEquals(DownloadFileType.AndroidApp, result.fileType)
        assertTrue(result.isLikelyDownload)
        assertEquals("InstallerOrExecutable", result.resourceKind.name)
    }

    @Test
    fun `head method not allowed returns unavailable`() = runBlocking {
        val repository =
            KtorUrlMetadataRepository(
                httpClient = httpClient(status = HttpStatusCode.MethodNotAllowed),
            )

        val result = repository.fetchMetadata("https://example.com")

        assertEquals(UrlMetadataStatus.Unavailable, result.status)
        assertFalse(result.reasons.isEmpty())
    }

    @Test
    fun `non http url is normalized before head request`() = runBlocking {
        val repository =
            KtorUrlMetadataRepository(
                httpClient =
                httpClient(
                    status = HttpStatusCode.OK,
                    headers = listOf(HttpHeaders.ContentType to listOf(ContentType.Application.Pdf.toString())),
                    onRequest = { request ->
                        assertEquals(HttpMethod.Head, request.method)
                        assertEquals("https://example.com/report.pdf", request.url.toString())
                    },
                ),
            )

        val result = repository.fetchMetadata("example.com/report.pdf")

        assertEquals(UrlMetadataStatus.Available, result.status)
        assertEquals(DownloadFileType.Pdf, result.fileType)
    }

    @Test
    fun `filename star is preferred when content disposition is present`() = runBlocking {
        val repository =
            KtorUrlMetadataRepository(
                httpClient =
                httpClient(
                    status = HttpStatusCode.OK,
                    headers =
                    listOf(
                        HttpHeaders.ContentType to listOf(ContentType.Application.Pdf.toString()),
                        HttpHeaders.ContentDisposition to listOf("""attachment; filename*=report-from-server.pdf"""),
                    ),
                ),
            )

        val result = repository.fetchMetadata("https://example.com/ignored")

        assertEquals(UrlMetadataStatus.Available, result.status)
        assertEquals("report-from-server.pdf", result.fileName)
        assertEquals("pdf", result.fileExtension)
        assertEquals(DownloadFileType.Pdf, result.fileType)
        assertTrue(result.isLikelyDownload)
    }

    @Test
    fun `attachment disposition produces download metadata`() = runBlocking {
        val repository =
            KtorUrlMetadataRepository(
                httpClient =
                httpClient(
                    status = HttpStatusCode.OK,
                    headers =
                    listOf(
                        HttpHeaders.ContentType to listOf(ContentType.Application.Pdf.toString()),
                        HttpHeaders.ContentDisposition to listOf("""attachment; filename="menu.pdf""""),
                    ),
                ),
            )

        val result = repository.fetchMetadata("https://example.com/download")

        assertEquals(UrlMetadataStatus.Available, result.status)
        assertEquals("menu.pdf", result.fileName)
        assertEquals("pdf", result.fileExtension)
        assertEquals(DownloadFileType.Pdf, result.fileType)
        assertTrue(result.isLikelyDownload)
        assertEquals("Document", result.resourceKind.name)
    }

    @Test
    fun `octet stream keeps unknown file type but marks download as likely`() = runBlocking {
        val repository =
            KtorUrlMetadataRepository(
                httpClient =
                httpClient(
                    status = HttpStatusCode.OK,
                    headers =
                    listOf(
                        HttpHeaders.ContentType to listOf("application/octet-stream"),
                    ),
                ),
            )

        val result = repository.fetchMetadata("https://example.com/file")

        assertEquals(UrlMetadataStatus.Available, result.status)
        assertEquals(DownloadFileType.Unknown, result.fileType)
        assertTrue(result.isLikelyDownload)
        assertEquals("UnknownBinary", result.resourceKind.name)
    }

    @Test
    fun `simple url with html content does not invent file details`() = runBlocking {
        val repository =
            KtorUrlMetadataRepository(
                httpClient =
                httpClient(
                    status = HttpStatusCode.OK,
                    headers = listOf(HttpHeaders.ContentType to listOf(ContentType.Text.Html.toString())),
                ),
            )

        val result = repository.fetchMetadata("https://example.com")

        assertEquals(UrlMetadataStatus.Available, result.status)
        assertEquals(null, result.finalUrl)
        assertEquals("text/html", result.contentType)
        assertEquals(null, result.fileName)
        assertEquals(null, result.fileExtension)
        assertEquals(DownloadFileType.Unknown, result.fileType)
        assertFalse(result.isLikelyDownload)
    }

    @Test
    fun `html path stays a web page and does not become a file`() = runBlocking {
        val repository =
            KtorUrlMetadataRepository(
                httpClient =
                httpClient(
                    status = HttpStatusCode.OK,
                    headers = listOf(HttpHeaders.ContentType to listOf(ContentType.Text.Html.toString())),
                ),
            )

        val result = repository.fetchMetadata("https://example.com/about.html")

        assertEquals(UrlMetadataStatus.Available, result.status)
        assertEquals(null, result.fileName)
        assertEquals(null, result.fileExtension)
        assertEquals(DownloadFileType.Unknown, result.fileType)
        assertFalse(result.isLikelyDownload)
    }

    @Test
    fun `download path with html content stays a web page`() = runBlocking {
        val repository =
            KtorUrlMetadataRepository(
                httpClient =
                httpClient(
                    status = HttpStatusCode.OK,
                    headers = listOf(HttpHeaders.ContentType to listOf(ContentType.Text.Html.toString())),
                ),
            )

        val result = repository.fetchMetadata("https://example.com/download")

        assertEquals(UrlMetadataStatus.Available, result.status)
        assertEquals(null, result.fileName)
        assertEquals(null, result.fileExtension)
        assertEquals(DownloadFileType.Unknown, result.fileType)
        assertFalse(result.isLikelyDownload)
    }

    @Test
    fun `pdf path is inferred as a document when head is empty`() = runBlocking {
        val repository =
            KtorUrlMetadataRepository(
                httpClient =
                httpClient(
                    status = HttpStatusCode.OK,
                    headers = emptyList(),
                ),
            )

        val result = repository.fetchMetadata("https://example.com/menu.pdf")

        assertEquals(UrlMetadataStatus.Available, result.status)
        assertEquals("menu.pdf", result.fileName)
        assertEquals("pdf", result.fileExtension)
        assertEquals(DownloadFileType.Pdf, result.fileType)
        assertEquals("Document", result.resourceKind.name)
        assertFalse(result.isLikelyDownload)
    }

    @Test
    fun `apk path is treated as android app download`() = runBlocking {
        val repository =
            KtorUrlMetadataRepository(
                httpClient =
                httpClient(
                    status = HttpStatusCode.OK,
                    headers = listOf(HttpHeaders.ContentType to listOf("application/vnd.android.package-archive")),
                ),
            )

        val result = repository.fetchMetadata("https://example.com/app.apk")

        assertEquals(UrlMetadataStatus.Available, result.status)
        assertEquals("app.apk", result.fileName)
        assertEquals("apk", result.fileExtension)
        assertEquals(DownloadFileType.AndroidApp, result.fileType)
        assertTrue(result.isLikelyDownload)
        assertEquals("InstallerOrExecutable", result.resourceKind.name)
    }

    private fun httpClient(
        status: HttpStatusCode,
        headers: List<Pair<String, List<String>>> = emptyList(),
        onRequest: ((io.ktor.client.request.HttpRequestData) -> Unit)? = null,
    ): HttpClient {
        val engine =
            MockEngine { request ->
                onRequest?.invoke(request)
                respond(
                    content = "",
                    status = status,
                    headers = headersOf(*headers.toTypedArray()),
                )
            }

        return HttpClient(engine)
    }
}

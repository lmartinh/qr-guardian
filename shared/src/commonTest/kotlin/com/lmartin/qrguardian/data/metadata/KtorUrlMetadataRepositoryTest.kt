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
        val repository = KtorUrlMetadataRepository(
            httpClient = httpClient(
                status = HttpStatusCode.OK,
                headers = listOf(
                    HttpHeaders.ContentType to listOf("application/vnd.android.package-archive; charset=utf-8"),
                    HttpHeaders.ContentDisposition to listOf("""attachment; filename="installer.apk""""),
                    HttpHeaders.ContentLength to listOf("2048")
                )
            )
        )

        val result = repository.fetchMetadata("example.com/download")

        assertEquals(UrlMetadataStatus.Available, result.status)
        assertEquals("https://example.com/download", result.finalUrl)
        assertEquals("application/vnd.android.package-archive", result.contentType)
        assertEquals("""attachment; filename="installer.apk"""", result.contentDisposition)
        assertEquals(2048L, result.contentLength)
        assertEquals("installer.apk", result.fileName)
        assertEquals("apk", result.fileExtension)
        assertEquals(DownloadFileType.AndroidApp, result.fileType)
        assertTrue(result.isLikelyDownload)
    }

    @Test
    fun `head method not allowed returns unavailable`() = runBlocking {
        val repository = KtorUrlMetadataRepository(
            httpClient = httpClient(status = HttpStatusCode.MethodNotAllowed)
        )

        val result = repository.fetchMetadata("https://example.com")

        assertEquals(UrlMetadataStatus.Unavailable, result.status)
        assertFalse(result.reasons.isEmpty())
    }

    @Test
    fun `non http url is normalized before head request`() = runBlocking {
        val repository = KtorUrlMetadataRepository(
            httpClient = httpClient(
                status = HttpStatusCode.OK,
                headers = listOf(HttpHeaders.ContentType to listOf(ContentType.Application.Pdf.toString())),
                onRequest = { request ->
                    assertEquals(HttpMethod.Head, request.method)
                    assertEquals("https://example.com/report.pdf", request.url.toString())
                }
            )
        )

        val result = repository.fetchMetadata("example.com/report.pdf")

        assertEquals(UrlMetadataStatus.Available, result.status)
        assertEquals(DownloadFileType.Pdf, result.fileType)
    }

    private fun httpClient(
        status: HttpStatusCode,
        headers: List<Pair<String, List<String>>> = emptyList(),
        onRequest: ((io.ktor.client.request.HttpRequestData) -> Unit)? = null
    ): HttpClient {
        val engine = MockEngine { request ->
            onRequest?.invoke(request)
            respond(
                content = "",
                status = status,
                headers = headersOf(*headers.toTypedArray())
            )
        }

        return HttpClient(engine)
    }
}

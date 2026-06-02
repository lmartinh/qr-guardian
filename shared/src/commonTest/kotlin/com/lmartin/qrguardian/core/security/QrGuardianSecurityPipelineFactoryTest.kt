package com.lmartin.qrguardian.core.security

import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
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

class QrGuardianSecurityPipelineFactoryTest {
    @Test
    fun `factory wires the security pipeline`() = runBlocking {
        val useCase = QrGuardianSecurityPipelineFactory.createAnalyzeQrSafetyUseCase(
            httpClient = httpClient()
        )

        val result = useCase("example.com/report.pdf")

        assertEquals(QrContentType.Url, result.contentType)
        assertEquals(SecurityLevel.Suspicious, result.localScan.level)
        assertEquals(ScanStatus.NotConfigured, result.remoteReputation.status)
        assertTrue(result.canOpen)
        assertTrue(result.localScan.metadata.any { it.label == "File type" && it.value == "PDF" })
        assertFalse(result.localScan.metadata.isEmpty())
    }

    private fun httpClient(): HttpClient {
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Head, request.method)
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType to listOf(ContentType.Application.Pdf.toString()),
                    HttpHeaders.ContentDisposition to listOf("""attachment; filename*=report-from-server.pdf""")
                )
            )
        }

        return HttpClient(engine)
    }
}

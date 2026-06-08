package com.lmartin.qrguardian.core.security

import com.lmartin.qrguardian.data.reputation.RemoteReputationConfig
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
    fun `factory creates analyze use case with empty config and local only behavior`() = runBlocking {
        val useCase =
            QrGuardianSecurityPipelineFactory.create(
                httpClient = localOnlyHttpClient(),
                remoteReputationConfig = RemoteReputationConfig(),
            )

        val result = useCase("example.com/report.pdf")

        assertEquals(QrContentType.Url, result.contentType)
        assertEquals(SecurityLevel.Suspicious, result.localScan.level)
        assertEquals(ScanStatus.NotConfigured, result.remoteReputation.status)
        assertTrue(result.canOpen)
        assertTrue(result.localScan.metadata.any { it.label == "File type" && it.value == "PDF" })
        assertFalse(result.localScan.metadata.isEmpty())
    }

    @Test
    fun `factory uses google reputation when google key is configured`() = runBlocking {
        val useCase =
            QrGuardianSecurityPipelineFactory.create(
                httpClient = providerHttpClient(),
                remoteReputationConfig = RemoteReputationConfig(googleSafeBrowsingApiKey = "google-key"),
            )

        val result = useCase("example.com/report.pdf")

        assertEquals(QrContentType.Url, result.contentType)
        assertEquals(ScanStatus.Completed, result.remoteReputation.status)
        assertTrue(
            result.remoteReputation.metadata.any { it.label == "Provider" && it.value.contains("Google Safe Browsing") },
        )
        assertTrue(result.localScan.metadata.any { it.label == "File type" && it.value == "PDF" })
    }

    @Test
    fun `factory uses urlhaus reputation when urlhaus key is configured`() = runBlocking {
        val useCase =
            QrGuardianSecurityPipelineFactory.create(
                httpClient = providerHttpClient(),
                remoteReputationConfig = RemoteReputationConfig(urlHausApiKey = "urlhaus-key"),
            )

        val result = useCase("example.com/report.pdf")

        assertEquals(QrContentType.Url, result.contentType)
        assertEquals(ScanStatus.Completed, result.remoteReputation.status)
        assertTrue(result.remoteReputation.metadata.any { it.label == "Provider" && it.value.contains("URLhaus") })
        assertTrue(result.localScan.metadata.any { it.label == "File type" && it.value == "PDF" })
    }

    @Test
    fun `factory merges both providers when both keys are configured`() = runBlocking {
        val useCase =
            QrGuardianSecurityPipelineFactory.create(
                httpClient = providerHttpClient(),
                remoteReputationConfig =
                RemoteReputationConfig(
                    googleSafeBrowsingApiKey = "google-key",
                    urlHausApiKey = "urlhaus-key",
                ),
            )

        val result = useCase("example.com/report.pdf")

        assertEquals(QrContentType.Url, result.contentType)
        assertEquals(ScanStatus.Completed, result.remoteReputation.status)
        assertTrue(
            result.remoteReputation.metadata.any { it.label == "Provider" && it.value == "Google Safe Browsing, URLhaus" },
        )
        assertTrue(result.localScan.metadata.any { it.label == "File type" && it.value == "PDF" })
    }

    private fun localOnlyHttpClient(): HttpClient {
        val engine =
            MockEngine { request ->
                when (request.method) {
                    HttpMethod.Head -> {
                        respond(
                            content = "",
                            status = HttpStatusCode.OK,
                            headers =
                            headersOf(
                                HttpHeaders.ContentType to listOf(ContentType.Application.Pdf.toString()),
                                HttpHeaders.ContentDisposition to listOf("""attachment; filename*=report-from-server.pdf"""),
                            ),
                        )
                    }

                    HttpMethod.Post -> {
                        error("Unexpected remote reputation request in local-only mode.")
                    }

                    else -> {
                        error("Unexpected HTTP method: ${request.method}")
                    }
                }
            }

        return HttpClient(engine)
    }

    private fun providerHttpClient(): HttpClient {
        val engine =
            MockEngine { request ->
                when (request.method) {
                    HttpMethod.Head -> {
                        respond(
                            content = "",
                            status = HttpStatusCode.OK,
                            headers =
                            headersOf(
                                HttpHeaders.ContentType to listOf(ContentType.Application.Pdf.toString()),
                                HttpHeaders.ContentDisposition to listOf("""attachment; filename*=report-from-server.pdf"""),
                            ),
                        )
                    }

                    HttpMethod.Post -> {
                        respond(
                            content =
                            when (request.url.host) {
                                "safebrowsing.googleapis.com" -> """{"matches":[]}"""
                                "urlhaus-api.abuse.ch" -> """{"query_status":"no_results"}"""
                                else -> error("Unexpected host: ${request.url.host}")
                            },
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    }

                    else -> {
                        error("Unexpected HTTP method: ${request.method}")
                    }
                }
            }

        return HttpClient(engine)
    }
}

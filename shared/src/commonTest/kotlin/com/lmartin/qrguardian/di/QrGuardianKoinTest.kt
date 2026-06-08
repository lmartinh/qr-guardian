package com.lmartin.qrguardian.di

import com.lmartin.qrguardian.core.security.QrGuardianSecurityPipelineFactory
import com.lmartin.qrguardian.data.reputation.RemoteReputationConfig
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.usecase.AnalyzeQrSafetyUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class QrGuardianKoinTest {
    @Test
    fun `koin starts with empty remote config and resolves the use case`() = runBlocking {
        val koinApp = initKoin(
            remoteReputationConfig = RemoteReputationConfig(),
            additionalModules = listOf(localOnlyHttpClientModule()),
        )

        try {
            val resolvedConfig = koinApp.koin.get<RemoteReputationConfig>()
            val resolvedUseCase = koinApp.koin.get<AnalyzeQrSafetyUseCase>()
            val factoryUseCase = QrGuardianSecurityPipelineFactory.create(
                httpClient = koinApp.koin.get(),
                remoteReputationConfig = resolvedConfig,
            )

            assertEquals(RemoteReputationConfig(), resolvedConfig)
            assertEquals(
                factoryUseCase("example.com/report.pdf"),
                resolvedUseCase("example.com/report.pdf"),
            )
        } finally {
            koinApp.close()
        }
    }

    @Test
    fun `koin with empty config keeps remote reputation not configured for url`() = runBlocking {
        val koinApp = initKoin(
            remoteReputationConfig = RemoteReputationConfig(),
            additionalModules = listOf(localOnlyHttpClientModule()),
        )

        try {
            val useCase = koinApp.koin.get<AnalyzeQrSafetyUseCase>()
            val result = useCase("example.com/report.pdf")

            assertEquals(ScanStatus.NotConfigured, result.remoteReputation.status)
            assertTrue(result.localScan.metadata.any { it.label == "File type" && it.value == "PDF" })
        } finally {
            koinApp.close()
        }
    }

    @Test
    fun `koin resolves configured google pipeline without real network calls`() = runBlocking {
        val koinApp = initKoin(
            remoteReputationConfig = RemoteReputationConfig(googleSafeBrowsingApiKey = "google-key"),
            additionalModules = listOf(providerHttpClientModule()),
        )

        try {
            val useCase = koinApp.koin.get<AnalyzeQrSafetyUseCase>()
            val result = useCase("example.com/report.pdf")

            assertEquals(ScanStatus.Completed, result.remoteReputation.status)
            assertTrue(result.remoteReputation.metadata.any { it.label == "Provider" && it.value.contains("Google Safe Browsing") })
        } finally {
            koinApp.close()
        }
    }

    @Test
    fun `koin override behavior still works`() = runBlocking {
        val koinApp = initKoin(
            remoteReputationConfig = RemoteReputationConfig(googleSafeBrowsingApiKey = "google-key"),
            additionalModules = listOf(
                module {
                    single<HttpClient> {
                        HttpClient(
                            MockEngine { request ->
                                when (request.method) {
                                    HttpMethod.Head -> respond(
                                        content = "",
                                        status = HttpStatusCode.OK,
                                        headers = headersOf(
                                            HttpHeaders.ContentType to listOf(ContentType.Application.Pdf.toString()),
                                            HttpHeaders.ContentDisposition to listOf("""attachment; filename*=override.pdf"""),
                                        ),
                                    )

                                    HttpMethod.Post -> respond(
                                        content = """{"matches":[{"threatType":"MALWARE"}]}""",
                                        status = HttpStatusCode.OK,
                                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                                    )

                                    else -> error("Unexpected HTTP method: ${request.method}")
                                }
                            },
                        )
                    }
                },
            ),
        )

        try {
            val useCase = koinApp.koin.get<AnalyzeQrSafetyUseCase>()
            val result = useCase("https://override.example.com")

            assertEquals(ScanStatus.Completed, result.remoteReputation.status)
            assertTrue(result.remoteReputation.metadata.any { it.label == "Provider" && it.value == "Google Safe Browsing" })
        } finally {
            koinApp.close()
        }
    }

    @Test
    fun `koin starts without api keys`() {
        val koinApp = initKoin(
            remoteReputationConfig = RemoteReputationConfig(),
            additionalModules = listOf(localOnlyHttpClientModule()),
        )

        try {
            assertIs<AnalyzeQrSafetyUseCase>(koinApp.koin.get<AnalyzeQrSafetyUseCase>())
        } finally {
            koinApp.close()
        }
    }

    private fun localOnlyHttpClientModule() = module {
        single<HttpClient> {
            HttpClient(
                MockEngine { request ->
                    when (request.method) {
                        HttpMethod.Head -> respond(
                            content = "",
                            status = HttpStatusCode.OK,
                            headers = headersOf(
                                HttpHeaders.ContentType to listOf(ContentType.Application.Pdf.toString()),
                                HttpHeaders.ContentDisposition to listOf("""attachment; filename*=report.pdf"""),
                            ),
                        )

                        HttpMethod.Post -> error("Unexpected remote reputation request in local-only mode.")

                        else -> error("Unexpected HTTP method: ${request.method}")
                    }
                },
            )
        }
    }

    private fun providerHttpClientModule() = module {
        single<HttpClient> {
            HttpClient(
                MockEngine { request ->
                    when (request.method) {
                        HttpMethod.Head -> respond(
                            content = "",
                            status = HttpStatusCode.OK,
                            headers = headersOf(
                                HttpHeaders.ContentType to listOf(ContentType.Application.Pdf.toString()),
                                HttpHeaders.ContentDisposition to listOf("""attachment; filename*=report.pdf"""),
                            ),
                        )

                        HttpMethod.Post -> respond(
                            content = when (request.url.host) {
                                "safebrowsing.googleapis.com" -> """{"matches":[]}"""
                                "urlhaus-api.abuse.ch" -> """{"query_status":"no_results"}"""
                                else -> error("Unexpected host: ${request.url.host}")
                            },
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )

                        else -> error("Unexpected HTTP method: ${request.method}")
                    }
                },
            )
        }
    }
}

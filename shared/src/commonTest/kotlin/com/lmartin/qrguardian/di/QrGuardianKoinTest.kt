package com.lmartin.qrguardian.di

import com.lmartin.qrguardian.data.reputation.NoOpUrlReputationRepository
import com.lmartin.qrguardian.data.reputation.RemoteReputationConfig
import com.lmartin.qrguardian.data.reputation.UrlHausReputationRepository
import com.lmartin.qrguardian.data.reputation.google.GoogleSafeBrowsingUrlReputationRepository
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.reputation.ThreatCategory
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
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

class QrGuardianKoinTest {
    @Test
    fun `empty remote config resolves local only pipeline`() = runBlocking {
        val koinApp = initKoin(
            remoteReputationConfig = RemoteReputationConfig(),
            additionalModules = listOf(localOnlyHttpClientModule())
        )

        try {
            val remoteConfig = koinApp.koin.get<RemoteReputationConfig>()
            val useCase = koinApp.koin.get<AnalyzeQrSafetyUseCase>()

            assertEquals(RemoteReputationConfig(), remoteConfig)
            assertIs<NoOpUrlReputationRepository>(koinApp.koin.get<UrlReputationRepository>())

            val result = useCase("example.com/report.pdf")

            assertEquals(ScanStatus.NotConfigured, result.remoteReputation.status)
        } finally {
            koinApp.close()
        }
    }

    @Test
    fun `google key resolves google reputation repository without real network calls`() = runBlocking {
        val koinApp = initKoin(
            remoteReputationConfig = RemoteReputationConfig(googleSafeBrowsingApiKey = "google-key"),
            additionalModules = listOf(reputationHttpClientModule())
        )

        try {
            val repository = koinApp.koin.get<UrlReputationRepository>()

            assertIs<GoogleSafeBrowsingUrlReputationRepository>(repository)
            assertEquals(
                UrlReputationResult(
                    status = UrlReputationStatus.Clean,
                    provider = "Google Safe Browsing",
                    categories = emptyList(),
                    reasons = listOf("No threats were reported by Google Safe Browsing.")
                ),
                repository.checkUrl("https://example.com")
            )
        } finally {
            koinApp.close()
        }
    }

    @Test
    fun `urlhaus key resolves urlhaus reputation repository without real network calls`() = runBlocking {
        val koinApp = initKoin(
            remoteReputationConfig = RemoteReputationConfig(urlHausApiKey = "urlhaus-key"),
            additionalModules = listOf(reputationHttpClientModule())
        )

        try {
            val repository = koinApp.koin.get<UrlReputationRepository>()

            assertIs<UrlHausReputationRepository>(repository)
            assertEquals(
                UrlReputationResult(
                    status = UrlReputationStatus.Clean,
                    provider = "URLhaus",
                    categories = emptyList(),
                    reasons = listOf("No threats were reported by URLhaus.")
                ),
                repository.checkUrl("https://example.com")
            )
        } finally {
            koinApp.close()
        }
    }

    @Test
    fun `test modules can override the url reputation repository`() = runBlocking {
        val koinApp = initKoin(
            remoteReputationConfig = RemoteReputationConfig(googleSafeBrowsingApiKey = "google-key"),
            additionalModules = listOf(
                reputationHttpClientModule(),
                module {
                    single<UrlReputationRepository> {
                        object : UrlReputationRepository {
                            override suspend fun checkUrl(url: String): UrlReputationResult {
                                return UrlReputationResult(
                                    status = UrlReputationStatus.Malicious,
                                    provider = "Fake repo",
                                    categories = listOf(ThreatCategory.Phishing),
                                    reasons = listOf("Overridden in test.")
                                )
                            }
                        }
                    }
                }
            )
        )

        try {
            val useCase = koinApp.koin.get<AnalyzeQrSafetyUseCase>()
            val result = useCase("https://example.com")

            assertEquals(ScanStatus.Completed, result.remoteReputation.status)
            assertEquals("Fake repo", result.remoteReputation.metadata.first().value)
            assertEquals(
                UrlReputationStatus.Malicious,
                koinApp.koin.get<UrlReputationRepository>().checkUrl("https://example.com").status
            )
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
                                HttpHeaders.ContentDisposition to listOf("""attachment; filename*=report.pdf""")
                            )
                        )

                        HttpMethod.Post -> error("Unexpected remote reputation request in local-only mode.")

                        else -> error("Unexpected HTTP method: ${request.method}")
                    }
                }
            )
        }
    }

    private fun reputationHttpClientModule() = module {
        single<HttpClient> {
            HttpClient(
                MockEngine { request ->
                    when (request.method) {
                        HttpMethod.Head -> respond(
                            content = "",
                            status = HttpStatusCode.OK,
                            headers = headersOf(
                                HttpHeaders.ContentType to listOf(ContentType.Application.Pdf.toString()),
                                HttpHeaders.ContentDisposition to listOf("""attachment; filename*=report.pdf""")
                            )
                        )

                        HttpMethod.Post -> respond(
                            content = when (request.url.host) {
                                "safebrowsing.googleapis.com" -> """{"matches":[]}"""
                                "urlhaus-api.abuse.ch" -> """{"query_status":"no_results"}"""
                                else -> error("Unexpected host: ${request.url.host}")
                            },
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        )

                        else -> error("Unexpected HTTP method: ${request.method}")
                    }
                }
            )
        }
    }
}

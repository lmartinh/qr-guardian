package com.lmartin.qrguardian.data.reputation

import com.lmartin.qrguardian.data.reputation.google.GoogleSafeBrowsingUrlReputationRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlin.test.Test
import kotlin.test.assertIs

class UrlReputationRepositoryFactoryTest {
    @Test
    fun `empty config returns no op repository`() {
        val repository =
            UrlReputationRepositoryFactory.create(
                config = RemoteReputationConfig(),
                httpClient = testHttpClient(),
            )

        assertIs<NoOpUrlReputationRepository>(repository)
    }

    @Test
    fun `google key only returns google repository`() {
        val repository =
            UrlReputationRepositoryFactory.create(
                config = RemoteReputationConfig(googleSafeBrowsingApiKey = "google-key"),
                httpClient = testHttpClient(),
            )

        assertIs<GoogleSafeBrowsingUrlReputationRepository>(repository)
    }

    @Test
    fun `urlhaus key only returns urlhaus repository`() {
        val repository =
            UrlReputationRepositoryFactory.create(
                config = RemoteReputationConfig(urlHausApiKey = "urlhaus-key"),
                httpClient = testHttpClient(),
            )

        assertIs<UrlHausReputationRepository>(repository)
    }

    @Test
    fun `both keys return composite repository`() {
        val repository =
            UrlReputationRepositoryFactory.create(
                config =
                RemoteReputationConfig(
                    googleSafeBrowsingApiKey = "google-key",
                    urlHausApiKey = "urlhaus-key",
                ),
                httpClient = testHttpClient(),
            )

        assertIs<CompositeUrlReputationRepository>(repository)
    }

    private fun testHttpClient(): HttpClient = HttpClient(MockEngine { error("This factory test should not perform network requests.") })
}

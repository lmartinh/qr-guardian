package com.lmartin.qrguardian.data.reputation.google

import com.lmartin.qrguardian.domain.reputation.ThreatCategory
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoogleSafeBrowsingUrlReputationRepositoryTest {
    @Test
    fun `api key empty returns not configured`() = runBlocking {
        val repository =
            GoogleSafeBrowsingUrlReputationRepository(
                httpClient = httpClientWithResponse("{}"),
                apiKey = "",
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.NotConfigured, result.status)
        assertEquals("Google Safe Browsing", result.provider)
        assertTrue(result.reasons.contains("Google Safe Browsing API key is not configured."))
    }

    @Test
    fun `empty response is clean`() = runBlocking {
        val repository =
            GoogleSafeBrowsingUrlReputationRepository(
                httpClient = httpClientWithResponse("{}"),
                apiKey = "test-key",
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Clean, result.status)
        assertEquals("Google Safe Browsing", result.provider)
        assertTrue(result.categories.isEmpty())
        assertTrue(result.reasons.contains("No threats were reported by Google Safe Browsing."))
    }

    @Test
    fun `social engineering response is malicious`() = runBlocking {
        val repository =
            GoogleSafeBrowsingUrlReputationRepository(
                httpClient =
                httpClientWithResponse(
                    """{"matches":[{"threatType":"SOCIAL_ENGINEERING"}]}""",
                ),
                apiKey = "test-key",
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Malicious, result.status)
        assertEquals(listOf(ThreatCategory.Phishing), result.categories)
        assertTrue(result.reasons.contains("Google Safe Browsing reported this URL as SOCIAL_ENGINEERING."))
    }

    @Test
    fun `malware response is malicious`() = runBlocking {
        val repository =
            GoogleSafeBrowsingUrlReputationRepository(
                httpClient = httpClientWithResponse("""{"matches":[{"threatType":"MALWARE"}]}"""),
                apiKey = "test-key",
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Malicious, result.status)
        assertEquals(listOf(ThreatCategory.Malware), result.categories)
        assertTrue(result.reasons.contains("Google Safe Browsing reported this URL as MALWARE."))
    }

    @Test
    fun `unwanted software and potentially harmful application map to distinct categories`() = runBlocking {
        val repository =
            GoogleSafeBrowsingUrlReputationRepository(
                httpClient =
                httpClientWithResponse(
                    """{"matches":[{"threatType":"UNWANTED_SOFTWARE"},{"threatType":"POTENTIALLY_HARMFUL_APPLICATION"}]}""",
                ),
                apiKey = "test-key",
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Malicious, result.status)
        assertEquals(
            listOf(ThreatCategory.UnwantedSoftware, ThreatCategory.Malware),
            result.categories,
        )
        assertEquals(2, result.reasons.size)
    }

    @Test
    fun `google malware test url is malicious`() = runBlocking {
        val repository =
            GoogleSafeBrowsingUrlReputationRepository(
                httpClient = httpClientWithResponse("""{"matches":[{"threatType":"MALWARE"}]}"""),
                apiKey = "test-key",
            )

        val result = repository.checkUrl("http://malware.testing.google.test/testing/malware/")

        assertEquals(UrlReputationStatus.Malicious, result.status)
        assertEquals(listOf(ThreatCategory.Malware), result.categories)
        assertTrue(result.reasons.contains("Google Safe Browsing reported this URL as MALWARE."))
    }

    @Test
    fun `http error returns error`() = runBlocking {
        val repository =
            GoogleSafeBrowsingUrlReputationRepository(
                httpClient = httpClientWithError(),
                apiKey = "test-key",
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Error, result.status)
        assertEquals("Google Safe Browsing", result.provider)
        assertTrue(result.reasons.contains("Google Safe Browsing check is currently unavailable."))
    }

    @Test
    fun `exception returns error without throwing`() = runBlocking {
        val repository =
            GoogleSafeBrowsingUrlReputationRepository(
                httpClient = HttpClient(MockEngine { throw IllegalStateException("boom") }),
                apiKey = "test-key",
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Error, result.status)
        assertFalse(result.reasons.isEmpty())
    }

    @Test
    fun `malformed payload returns error without false confidence`() = runBlocking {
        val repository =
            GoogleSafeBrowsingUrlReputationRepository(
                httpClient =
                httpClientWithResponse("{\"matches\":[{\"threatType\":\""),
                apiKey = "test-key",
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Error, result.status)
        assertEquals("Google Safe Browsing", result.provider)
        assertTrue(result.categories.isEmpty())
        assertTrue(result.reasons.contains("Google Safe Browsing check is currently unavailable."))
    }

    @Test
    fun `multiple matches keep distinct categories and reasons`() = runBlocking {
        val repository =
            GoogleSafeBrowsingUrlReputationRepository(
                httpClient =
                httpClientWithResponse(
                    """{"matches":[{"threatType":"MALWARE"},{"threatType":"UNWANTED_SOFTWARE"},{"threatType":"MALWARE"},{"threatType":"UNKNOWN_THREAT"}]}""",
                ),
                apiKey = "test-key",
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Malicious, result.status)
        assertEquals(
            listOf(ThreatCategory.Malware, ThreatCategory.UnwantedSoftware, ThreatCategory.Unknown),
            result.categories,
        )
        assertEquals(3, result.reasons.size)
    }

    private fun httpClientWithResponse(responseBody: String): HttpClient {
        val engine =
            MockEngine {
                respond(
                    content = responseBody,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        return HttpClient(engine)
    }

    private fun httpClientWithError(): HttpClient {
        val engine =
            MockEngine {
                respondError(HttpStatusCode.InternalServerError)
            }

        return HttpClient(engine)
    }
}

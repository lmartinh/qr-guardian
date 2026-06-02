package com.lmartin.qrguardian.data.reputation

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

class UrlHausReputationRepositoryTest {
    @Test
    fun `api key empty returns not configured`() = runBlocking {
        val repository = UrlHausReputationRepository(
            httpClient = httpClientWithResponse("{}"),
            apiKey = ""
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.NotConfigured, result.status)
        assertEquals("URLhaus", result.provider)
        assertTrue(result.reasons.contains("URLhaus API key is not configured."))
    }

    @Test
    fun `no results response is clean`() = runBlocking {
        val repository = UrlHausReputationRepository(
            httpClient = httpClientWithResponse("""{"query_status":"no_results"}"""),
            apiKey = "test-key"
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Clean, result.status)
        assertTrue(result.categories.isEmpty())
        assertTrue(result.reasons.contains("No threats were reported by URLhaus."))
    }

    @Test
    fun `malware response is malicious`() = runBlocking {
        val repository = UrlHausReputationRepository(
            httpClient = httpClientWithResponse("""{"query_status":"ok","url_status":"online"}"""),
            apiKey = "test-key"
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Malicious, result.status)
        assertEquals(listOf(ThreatCategory.Malware), result.categories)
        assertTrue(result.reasons.contains("URLhaus reported this URL as malware-related."))
    }

    @Test
    fun `unknown response is unknown`() = runBlocking {
        val repository = UrlHausReputationRepository(
            httpClient = httpClientWithResponse("""{"query_status":"ok","url_status":"unknown"}"""),
            apiKey = "test-key"
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Unknown, result.status)
        assertEquals(listOf(ThreatCategory.Unknown), result.categories)
        assertTrue(result.reasons.contains("URLhaus returned an unknown result."))
    }

    @Test
    fun `http error returns error`() = runBlocking {
        val repository = UrlHausReputationRepository(
            httpClient = httpClientWithError(),
            apiKey = "test-key"
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Error, result.status)
        assertTrue(result.reasons.contains("URLhaus check is currently unavailable."))
    }

    @Test
    fun `exception returns error without throwing`() = runBlocking {
        val repository = UrlHausReputationRepository(
            httpClient = HttpClient(MockEngine { throw IllegalStateException("boom") }),
            apiKey = "test-key"
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Error, result.status)
        assertFalse(result.reasons.isEmpty())
    }

    private fun httpClientWithResponse(responseBody: String): HttpClient {
        val engine = MockEngine {
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        return HttpClient(engine)
    }

    private fun httpClientWithError(): HttpClient {
        val engine = MockEngine {
            respondError(HttpStatusCode.InternalServerError)
        }

        return HttpClient(engine)
    }
}

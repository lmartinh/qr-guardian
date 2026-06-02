package com.lmartin.qrguardian.data.reputation

import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoOpUrlReputationRepositoryTest {
    private val repository = NoOpUrlReputationRepository()

    @Test
    fun `returns not configured status`() = runBlocking {
        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.NotConfigured, result.status)
    }

    @Test
    fun `returns provider none`() = runBlocking {
        val result = repository.checkUrl("https://example.com")

        assertEquals("None", result.provider)
    }

    @Test
    fun `returns empty categories`() = runBlocking {
        val result = repository.checkUrl("https://example.com")

        assertTrue(result.categories.isEmpty())
    }

    @Test
    fun `returns not configured reason`() = runBlocking {
        val result = repository.checkUrl("https://example.com")

        assertTrue(result.reasons.contains("Remote reputation checks are not configured."))
    }
}

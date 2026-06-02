package com.lmartin.qrguardian.data.reputation

import com.lmartin.qrguardian.domain.reputation.ThreatCategory
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompositeUrlReputationRepositoryTest {
    @Test
    fun `empty repositories returns not configured`() = runBlocking {
        val repository = CompositeUrlReputationRepository(emptyList())

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.NotConfigured, result.status)
        assertTrue(result.reasons.contains("Remote reputation checks are not configured."))
    }

    @Test
    fun `clean plus clean returns clean`() = runBlocking {
        val repository = CompositeUrlReputationRepository(
            listOf(
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Clean,
                        provider = "Google Safe Browsing",
                        categories = emptyList(),
                        reasons = listOf("No threats were reported by Google Safe Browsing.")
                    )
                ),
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Clean,
                        provider = "URLhaus",
                        categories = emptyList(),
                        reasons = listOf("No threats were reported by URLhaus.")
                    )
                )
            )
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Clean, result.status)
    }

    @Test
    fun `clean plus malicious returns malicious`() = runBlocking {
        val repository = CompositeUrlReputationRepository(
            listOf(
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Clean,
                        provider = "Google Safe Browsing",
                        categories = emptyList(),
                        reasons = listOf("No threats were reported by Google Safe Browsing.")
                    )
                ),
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Malicious,
                        provider = "URLhaus",
                        categories = listOf(ThreatCategory.Malware),
                        reasons = listOf("URLhaus reported this URL as malware-related.")
                    )
                )
            )
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Malicious, result.status)
        assertTrue(result.categories.contains(ThreatCategory.Malware))
    }

    @Test
    fun `suspicious plus clean returns suspicious`() = runBlocking {
        val repository = CompositeUrlReputationRepository(
            listOf(
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Suspicious,
                        provider = "Google Safe Browsing",
                        categories = listOf(ThreatCategory.Phishing),
                        reasons = listOf("Potential concern reported by Google Safe Browsing.")
                    )
                ),
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Clean,
                        provider = "URLhaus",
                        categories = emptyList(),
                        reasons = listOf("No threats were reported by URLhaus.")
                    )
                )
            )
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Suspicious, result.status)
    }

    @Test
    fun `error plus clean returns clean with error reason`() = runBlocking {
        val repository = CompositeUrlReputationRepository(
            listOf(
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Error,
                        provider = "Google Safe Browsing",
                        categories = emptyList(),
                        reasons = listOf("Google Safe Browsing check is currently unavailable.")
                    )
                ),
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Clean,
                        provider = "URLhaus",
                        categories = emptyList(),
                        reasons = listOf("No threats were reported by URLhaus.")
                    )
                )
            )
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Clean, result.status)
        assertTrue(result.reasons.contains("Google Safe Browsing check is currently unavailable."))
    }

    @Test
    fun `error plus error returns error`() = runBlocking {
        val repository = CompositeUrlReputationRepository(
            listOf(
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Error,
                        provider = "Google Safe Browsing",
                        categories = emptyList(),
                        reasons = listOf("Google Safe Browsing check is currently unavailable.")
                    )
                ),
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Error,
                        provider = "URLhaus",
                        categories = emptyList(),
                        reasons = listOf("URLhaus check is currently unavailable.")
                    )
                )
            )
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Error, result.status)
    }

    @Test
    fun `not configured plus clean returns clean`() = runBlocking {
        val repository = CompositeUrlReputationRepository(
            listOf(
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.NotConfigured,
                        provider = "None",
                        categories = emptyList(),
                        reasons = listOf("Remote reputation checks are not configured.")
                    )
                ),
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Clean,
                        provider = "URLhaus",
                        categories = emptyList(),
                        reasons = listOf("No threats were reported by URLhaus.")
                    )
                )
            )
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Clean, result.status)
    }

    @Test
    fun `does not duplicate reasons or categories`() = runBlocking {
        val repository = CompositeUrlReputationRepository(
            listOf(
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Malicious,
                        provider = "Google Safe Browsing",
                        categories = listOf(ThreatCategory.Malware),
                        reasons = listOf("Shared reason", "Shared reason")
                    )
                ),
                FakeUrlReputationRepository(
                    UrlReputationResult(
                        status = UrlReputationStatus.Malicious,
                        provider = "URLhaus",
                        categories = listOf(ThreatCategory.Malware),
                        reasons = listOf("Shared reason")
                    )
                )
            )
        )

        val result = repository.checkUrl("https://example.com")

        assertEquals(1, result.categories.count { it == ThreatCategory.Malware })
        assertEquals(1, result.reasons.count { it == "Shared reason" })
    }

    private class FakeUrlReputationRepository(
        private val result: UrlReputationResult
    ) : UrlReputationRepository {
        override suspend fun checkUrl(url: String): UrlReputationResult = result
    }
}

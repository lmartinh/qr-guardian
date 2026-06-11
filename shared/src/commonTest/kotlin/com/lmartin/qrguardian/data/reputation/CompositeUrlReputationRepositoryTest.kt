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
        val repository =
            CompositeUrlReputationRepository(
                listOf(
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Clean,
                            provider = "Google Safe Browsing",
                            categories = emptyList(),
                            reasons = listOf("No threats were reported by Google Safe Browsing."),
                        ),
                    ),
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Clean,
                            provider = "URLhaus",
                            categories = emptyList(),
                            reasons = listOf("No threats were reported by URLhaus."),
                        ),
                    ),
                ),
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Clean, result.status)
    }

    @Test
    fun `clean plus malicious returns malicious`() = runBlocking {
        val repository =
            CompositeUrlReputationRepository(
                listOf(
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Clean,
                            provider = "Google Safe Browsing",
                            categories = emptyList(),
                            reasons = listOf("No threats were reported by Google Safe Browsing."),
                        ),
                    ),
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Malicious,
                            provider = "URLhaus",
                            categories = listOf(ThreatCategory.Malware),
                            reasons = listOf("URLhaus reported this URL as malware-related."),
                        ),
                    ),
                ),
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Malicious, result.status)
        assertTrue(result.categories.contains(ThreatCategory.Malware))
    }

    @Test
    fun `suspicious plus clean returns suspicious`() = runBlocking {
        val repository =
            CompositeUrlReputationRepository(
                listOf(
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Suspicious,
                            provider = "Google Safe Browsing",
                            categories = listOf(ThreatCategory.Phishing),
                            reasons = listOf("Potential concern reported by Google Safe Browsing."),
                        ),
                    ),
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Clean,
                            provider = "URLhaus",
                            categories = emptyList(),
                            reasons = listOf("No threats were reported by URLhaus."),
                        ),
                    ),
                ),
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Suspicious, result.status)
    }

    @Test
    fun `error plus clean returns clean with error reason`() = runBlocking {
        val repository =
            CompositeUrlReputationRepository(
                listOf(
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Error,
                            provider = "Google Safe Browsing",
                            categories = emptyList(),
                            reasons = listOf("Google Safe Browsing check is currently unavailable."),
                        ),
                    ),
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Clean,
                            provider = "URLhaus",
                            categories = emptyList(),
                            reasons = listOf("No threats were reported by URLhaus."),
                        ),
                    ),
                ),
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Clean, result.status)
        assertTrue(result.reasons.contains("Google Safe Browsing check is currently unavailable."))
    }

    @Test
    fun `error plus error returns error`() = runBlocking {
        val repository =
            CompositeUrlReputationRepository(
                listOf(
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Error,
                            provider = "Google Safe Browsing",
                            categories = emptyList(),
                            reasons = listOf("Google Safe Browsing check is currently unavailable."),
                        ),
                    ),
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Error,
                            provider = "URLhaus",
                            categories = emptyList(),
                            reasons = listOf("URLhaus check is currently unavailable."),
                        ),
                    ),
                ),
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Error, result.status)
    }

    @Test
    fun `all not configured repositories returns not configured`() = runBlocking {
        val repository =
            CompositeUrlReputationRepository(
                listOf(
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.NotConfigured,
                            provider = "None",
                            categories = emptyList(),
                            reasons = listOf("Remote reputation checks are not configured."),
                        ),
                    ),
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.NotConfigured,
                            provider = "None",
                            categories = emptyList(),
                            reasons = listOf("Remote reputation checks are not configured."),
                        ),
                    ),
                ),
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.NotConfigured, result.status)
        assertEquals("Multiple providers", result.provider)
    }

    @Test
    fun `not configured plus clean returns clean`() = runBlocking {
        val repository =
            CompositeUrlReputationRepository(
                listOf(
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.NotConfigured,
                            provider = "None",
                            categories = emptyList(),
                            reasons = listOf("Remote reputation checks are not configured."),
                        ),
                    ),
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Clean,
                            provider = "URLhaus",
                            categories = emptyList(),
                            reasons = listOf("No threats were reported by URLhaus."),
                        ),
                    ),
                ),
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Clean, result.status)
    }

    @Test
    fun `distinct providers are joined when multiple named providers contribute`() = runBlocking {
        val repository =
            CompositeUrlReputationRepository(
                listOf(
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Clean,
                            provider = "Google Safe Browsing",
                            categories = emptyList(),
                            reasons = listOf("No threats were reported by Google Safe Browsing."),
                        ),
                    ),
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Clean,
                            provider = "URLhaus",
                            categories = emptyList(),
                            reasons = listOf("No threats were reported by URLhaus."),
                        ),
                    ),
                ),
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Clean, result.status)
        assertEquals("Google Safe Browsing, URLhaus", result.provider)
    }

    @Test
    fun `single named provider wins over blank provider labels`() = runBlocking {
        val repository =
            CompositeUrlReputationRepository(
                listOf(
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Clean,
                            provider = "",
                            categories = emptyList(),
                            reasons = listOf("Blank provider label."),
                        ),
                    ),
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Clean,
                            provider = "URLhaus",
                            categories = emptyList(),
                            reasons = listOf("No threats were reported by URLhaus."),
                        ),
                    ),
                ),
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Clean, result.status)
        assertEquals("URLhaus", result.provider)
    }

    @Test
    fun `does not duplicate reasons or categories`() = runBlocking {
        val repository =
            CompositeUrlReputationRepository(
                listOf(
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Malicious,
                            provider = "Google Safe Browsing",
                            categories = listOf(ThreatCategory.Malware),
                            reasons = listOf("Shared reason", "Shared reason"),
                        ),
                    ),
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Malicious,
                            provider = "URLhaus",
                            categories = listOf(ThreatCategory.Malware),
                            reasons = listOf("Shared reason"),
                        ),
                    ),
                ),
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(1, result.categories.count { it == ThreatCategory.Malware })
        assertEquals(1, result.reasons.count { it == "Shared reason" })
    }

    @Test
    fun `unknown status wins over clean and providers are collapsed when unnamed`() = runBlocking {
        val repository =
            CompositeUrlReputationRepository(
                listOf(
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Clean,
                            provider = "",
                            categories = emptyList(),
                            reasons = listOf("Clean"),
                        ),
                    ),
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Unknown,
                            provider = "Unknown",
                            categories = listOf(ThreatCategory.Unknown),
                            reasons = listOf("Unknown"),
                        ),
                    ),
                ),
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Unknown, result.status)
        assertEquals("Multiple providers", result.provider)
        assertTrue(result.categories.contains(ThreatCategory.Unknown))
        assertTrue(result.reasons.contains("Unknown"))
    }

    @Test
    fun `throwing repository is converted into an error result without stopping merge`() = runBlocking {
        val repository =
            CompositeUrlReputationRepository(
                listOf(
                    object : UrlReputationRepository {
                        override suspend fun checkUrl(url: String): UrlReputationResult {
                            throw IllegalStateException("boom")
                        }
                    },
                    FakeUrlReputationRepository(
                        UrlReputationResult(
                            status = UrlReputationStatus.Clean,
                            provider = "URLhaus",
                            categories = emptyList(),
                            reasons = listOf("No threats were reported by URLhaus."),
                        ),
                    ),
                ),
            )

        val result = repository.checkUrl("https://example.com")

        assertEquals(UrlReputationStatus.Clean, result.status)
        assertTrue(result.reasons.any { it.contains("currently unavailable") })
        assertEquals("URLhaus", result.provider)
    }

    private class FakeUrlReputationRepository(
        private val result: UrlReputationResult,
    ) : UrlReputationRepository {
        override suspend fun checkUrl(url: String): UrlReputationResult = result
    }
}

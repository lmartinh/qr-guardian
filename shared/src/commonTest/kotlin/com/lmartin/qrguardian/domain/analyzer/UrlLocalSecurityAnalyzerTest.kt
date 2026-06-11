package com.lmartin.qrguardian.domain.analyzer

import com.lmartin.qrguardian.domain.model.SecurityLevel
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UrlLocalSecurityAnalyzerTest {
    private val analyzer = UrlLocalSecurityAnalyzer()

    @Test
    fun `https url is safe`() {
        val result = analyzer.analyze("https://example.com")

        assertEquals(SecurityLevel.Safe, result.level)
        assertTrue(result.reasons.isEmpty())
    }

    @Test
    fun `http url is suspicious`() {
        val result = analyzer.analyze("http://example.com")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertContains(result.reasons, "The URL does not use HTTPS.")
    }

    @Test
    fun `url with at symbol is suspicious`() {
        val result = analyzer.analyze("https://google.com@evil.com")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("@ symbol") })
    }

    @Test
    fun `url with ip host is suspicious`() {
        val result = analyzer.analyze("https://192.168.1.20/login")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("IPv4 address") })
    }

    @Test
    fun `localhost url is suspicious and not treated as safe`() {
        val result = analyzer.analyze("http://localhost/admin")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("HTTPS") })
    }

    @Test
    fun `punycode url with login path is suspicious`() {
        val result = analyzer.analyze("https://xn--pple-43d.com/login")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("sensitive wording") })
    }

    @Test
    fun `credentials in url is suspicious because the real host is hidden after at symbol`() {
        val result = analyzer.analyze("https://google.com:password@example.com/login")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("@ symbol") })
    }

    @Test
    fun `url with shortener is suspicious`() {
        val result = analyzer.analyze("https://bit.ly/test")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("link shortener") })
    }

    @Test
    fun `url with dangerous extension is dangerous`() {
        val result = analyzer.analyze("https://example.com/file.apk")

        assertEquals(SecurityLevel.Dangerous, result.level)
        assertTrue(result.reasons.any { it.contains("dangerous file type") })
    }

    @Test
    fun `url with suspicious words is suspicious`() {
        val result = analyzer.analyze("https://example.com/login/verify")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("sensitive wording") })
    }

    @Test
    fun `very long url is suspicious`() {
        val longUrl =
            buildString {
                append("https://example.com/")
                repeat(301) {
                    append('a')
                }
            }

        val result = analyzer.analyze(longUrl)

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("longer than the local safety threshold") })
    }

    @Test
    fun `url with many query params is suspicious`() {
        val url =
            buildString {
                append("https://example.com?")
                repeat(9) { index ->
                    append("p")
                    append(index)
                    append("=")
                    append(index)
                    if (index < 8) {
                        append("&")
                    }
                }
            }

        val result = analyzer.analyze(url)

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("query parameters") })
    }

    @Test
    fun `safe triggered rule keeps the result safe`() {
        val analyzer =
            UrlLocalSecurityAnalyzer(
                rules =
                listOf(
                    object : com.lmartin.qrguardian.domain.rules.SecurityRule {
                        override fun evaluate(value: String) =
                            com.lmartin.qrguardian.domain.model.SecurityRuleResult(
                                triggered = true,
                                level = SecurityLevel.Safe,
                                reason = "Custom safe rule",
                            )
                    },
                ),
            )

        val result = analyzer.analyze("https://example.com")

        assertEquals(SecurityLevel.Safe, result.level)
        assertTrue(result.reasons.any { it == "Custom safe rule" })
    }

    @Test
    fun `brand impersonation is suspicious`() {
        val result = analyzer.analyze("paypal-secure-login.example.com")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("known brand") })
        assertFalse(result.reasons.isEmpty())
    }
}

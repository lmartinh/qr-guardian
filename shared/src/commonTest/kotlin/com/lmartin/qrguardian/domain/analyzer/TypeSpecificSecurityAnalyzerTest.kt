package com.lmartin.qrguardian.domain.analyzer

import com.lmartin.qrguardian.domain.model.SecurityLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypeSpecificSecurityAnalyzerTest {
    private val wifiAnalyzer = WifiLocalSecurityAnalyzer()
    private val sensitiveActionAnalyzer = SensitiveActionAnalyzer()
    private val plainTextSecurityAnalyzer = PlainTextSecurityAnalyzer()

    @Test
    fun `wifi open network is suspicious`() {
        val result = wifiAnalyzer.analyze("WIFI:T:nopass;S:OpenNetwork;;")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("open network") })
    }

    @Test
    fun `wifi with credentials is suspicious because it is sensitive`() {
        val result = wifiAnalyzer.analyze("WIFI:T:WPA;S:MyWifi;P:password;;")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("sensitive action") })
    }

    @Test
    fun `tel link is suspicious`() {
        val result = sensitiveActionAnalyzer.analyzePhone("tel:+34600000000")

        assertEquals(SecurityLevel.Suspicious, result.level)
    }

    @Test
    fun `sms with body is suspicious`() {
        val result = sensitiveActionAnalyzer.analyzeSms("sms:+34600000000?body=hello")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("predefined message body") })
    }

    @Test
    fun `sms with url in body adds an extra reason`() {
        val result = sensitiveActionAnalyzer.analyzeSms("sms:+34600000000?body=https://example.com")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("URL") })
    }

    @Test
    fun `mailto with predefined body is suspicious`() {
        val result = sensitiveActionAnalyzer.analyzeEmail("mailto:test@example.com?subject=Hello&body=World")

        assertEquals(SecurityLevel.Suspicious, result.level)
        assertTrue(result.reasons.any { it.contains("predefined content") })
    }

    @Test
    fun `plain text is unknown`() {
        val result = plainTextSecurityAnalyzer.analyze("hello world")

        assertEquals(SecurityLevel.Unknown, result.level)
        assertTrue(result.reasons.any { it.contains("plain text") })
    }
}

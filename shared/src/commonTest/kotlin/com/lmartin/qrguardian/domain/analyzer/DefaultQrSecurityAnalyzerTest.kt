package com.lmartin.qrguardian.domain.analyzer

import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.SecurityLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultQrSecurityAnalyzerTest {
    private val analyzer = DefaultQrSecurityAnalyzer()

    @Test
    fun `safe https url can be opened`() {
        val result = analyzer.analyze("https://example.com")

        assertEquals(QrContentType.Url, result.contentType)
        assertEquals(SecurityLevel.Safe, result.securityLevel)
        assertTrue(result.canOpen)
    }

    @Test
    fun `suspicious url can still be opened with warning`() {
        val result = analyzer.analyze("http://example.com")

        assertEquals(SecurityLevel.Suspicious, result.securityLevel)
        assertTrue(result.canOpen)
    }

    @Test
    fun `dangerous url cannot be opened`() {
        val result = analyzer.analyze("https://example.com/file.apk")

        assertEquals(SecurityLevel.Dangerous, result.securityLevel)
        assertFalse(result.canOpen)
    }

    @Test
    fun `plain text stays unknown`() {
        val result = analyzer.analyze("hello world")

        assertEquals(QrContentType.PlainText, result.contentType)
        assertEquals(SecurityLevel.Unknown, result.securityLevel)
        assertTrue(result.canOpen)
    }
}

package com.lmartin.qrguardian.domain.rules.url

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UrlParsingTest {
    @Test
    fun `parse url trims value and extracts components`() {
        val parsed = parseUrl("  https://user:pass@example.com:8443/path/file.pdf?x=1#section  ")

        assertEquals("https", parsed.scheme)
        assertEquals("example.com", parsed.host)
        assertEquals("/path/file.pdf", parsed.path)
        assertEquals("x=1", parsed.query)
        assertEquals("section", parsed.fragment)
        assertEquals("https://user:pass@example.com:8443/path/file.pdf?x=1#section", parsed.originalValue)
    }

    @Test
    fun `parse url without scheme still extracts host path query and fragment`() {
        val parsed = parseUrl("example.com/path/file.pdf?x=1#section")

        assertEquals(null, parsed.scheme)
        assertEquals("example.com", parsed.host)
        assertEquals("/path/file.pdf", parsed.path)
        assertEquals("x=1", parsed.query)
        assertEquals("section", parsed.fragment)
    }

    @Test
    fun `parsed url helpers detect ip query subdomains and dangerous extension`() {
        val parsedIp = parseUrl("https://192.168.1.10/login")
        val parsedNormal = parseUrl("https://example.com/path")
        val parsedQuery = parseUrl("https://example.com?a=1&b=2&c=3")
        val parsedSubdomains = parseUrl("https://a.b.c.d.e.example.com")
        val parsedDangerous = parseUrl("https://example.com/file.apk")

        assertTrue(parsedIp.isIpv4Host())
        assertFalse(parsedNormal.isIpv4Host())
        assertTrue(parsedQuery.hasTooManyQueryParameters(2))
        assertFalse(parsedQuery.hasTooManyQueryParameters(3))
        assertTrue(parsedSubdomains.hasTooManySubdomains(4))
        assertFalse(parsedNormal.hasTooManySubdomains(4))
        assertTrue(parsedDangerous.hasDangerousFileExtension(setOf(".apk", ".exe")))
        assertFalse(parsedNormal.hasDangerousFileExtension(setOf(".apk", ".exe")))
    }

    @Test
    fun `blank host and blank query stay safe in helper checks`() {
        val parsed = parseUrl("https:///path")

        assertEquals("", parsed.host)
        assertFalse(parsed.isIpv4Host())
        assertFalse(parsed.hasTooManyQueryParameters(0))
        assertFalse(parsed.hasTooManySubdomains(0))
    }
}

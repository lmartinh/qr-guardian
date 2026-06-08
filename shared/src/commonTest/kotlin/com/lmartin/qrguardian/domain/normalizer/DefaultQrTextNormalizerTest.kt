package com.lmartin.qrguardian.domain.normalizer

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultQrTextNormalizerTest {
    private val normalizer = DefaultQrTextNormalizer()

    @Test
    fun `normalizes text with spaces`() {
        assertEquals("hello world", normalizer.normalize("  hello world  "))
    }

    @Test
    fun `normalizes empty text`() {
        assertEquals("", normalizer.normalize("   "))
    }

    @Test
    fun `removes null characters`() {
        assertEquals("hello", normalizer.normalize("\u0000hello\u0000"))
    }

    @Test
    fun `preserves long text`() {
        val text =
            buildString {
                repeat(5000) {
                    append('a')
                }
            }

        assertEquals(text, normalizer.normalize(text))
    }
}

package com.lmartin.qrguardian.data.reputation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemoteReputationConfigFactoryTest {
    @Test
    fun `null values keep remote reputation disabled`() {
        val config = createRemoteReputationConfig(null, null)

        assertEquals(RemoteReputationConfig(), config)
        assertFalse(config.isGoogleSafeBrowsingEnabled)
        assertFalse(config.isUrlHausEnabled)
        assertFalse(config.hasAnyProviderEnabled)
    }

    @Test
    fun `blank values keep remote reputation disabled`() {
        val config = createRemoteReputationConfig("", "")

        assertEquals(RemoteReputationConfig(), config)
        assertFalse(config.isGoogleSafeBrowsingEnabled)
        assertFalse(config.isUrlHausEnabled)
        assertFalse(config.hasAnyProviderEnabled)
    }

    @Test
    fun `google key enables google only`() {
        val config = createRemoteReputationConfig("google-key", "")

        assertEquals("google-key", config.googleSafeBrowsingApiKey)
        assertEquals(null, config.urlHausApiKey)
        assertTrue(config.isGoogleSafeBrowsingEnabled)
        assertFalse(config.isUrlHausEnabled)
        assertTrue(config.hasAnyProviderEnabled)
    }

    @Test
    fun `urlhaus key enables urlhaus only`() {
        val config = createRemoteReputationConfig("", "urlhaus-key")

        assertEquals(null, config.googleSafeBrowsingApiKey)
        assertEquals("urlhaus-key", config.urlHausApiKey)
        assertFalse(config.isGoogleSafeBrowsingEnabled)
        assertTrue(config.isUrlHausEnabled)
        assertTrue(config.hasAnyProviderEnabled)
    }

    @Test
    fun `both keys enable both providers`() {
        val config = createRemoteReputationConfig("google-key", "urlhaus-key")

        assertEquals("google-key", config.googleSafeBrowsingApiKey)
        assertEquals("urlhaus-key", config.urlHausApiKey)
        assertTrue(config.isGoogleSafeBrowsingEnabled)
        assertTrue(config.isUrlHausEnabled)
        assertTrue(config.hasAnyProviderEnabled)
    }
}

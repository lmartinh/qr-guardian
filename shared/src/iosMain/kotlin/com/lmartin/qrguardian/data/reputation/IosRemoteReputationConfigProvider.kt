package com.lmartin.qrguardian.data.reputation

import platform.Foundation.NSBundle

internal object IosRemoteReputationConfigProvider {
    fun create(): RemoteReputationConfig {
        return createRemoteReputationConfig(
            googleSafeBrowsingApiKey = readBundleValue("GOOGLE_SAFE_BROWSING_API_KEY"),
            urlHausApiKey = readBundleValue("URLHAUS_API_KEY")
        )
    }

    private fun readBundleValue(key: String): String {
        return NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String ?: ""
    }
}

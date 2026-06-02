package com.lmartin.qrguardian.data.reputation

data class RemoteReputationConfig(
    val googleSafeBrowsingApiKey: String? = null,
    val urlHausApiKey: String? = null
) {
    val isGoogleSafeBrowsingEnabled: Boolean
        get() = !googleSafeBrowsingApiKey.isNullOrBlank()

    val isUrlHausEnabled: Boolean
        get() = !urlHausApiKey.isNullOrBlank()

    val hasAnyProviderEnabled: Boolean
        get() = isGoogleSafeBrowsingEnabled || isUrlHausEnabled
}

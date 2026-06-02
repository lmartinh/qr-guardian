package com.lmartin.qrguardian.data.reputation

data class RemoteReputationConfig(
    val googleSafeBrowsingApiKey: String? = null
) {
    val isGoogleSafeBrowsingEnabled: Boolean
        get() = !googleSafeBrowsingApiKey.isNullOrBlank()
}

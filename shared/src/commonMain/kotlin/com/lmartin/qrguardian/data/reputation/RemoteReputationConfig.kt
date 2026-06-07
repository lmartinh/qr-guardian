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

fun createRemoteReputationConfig(
    googleSafeBrowsingApiKey: String?,
    urlHausApiKey: String?
): RemoteReputationConfig {
    return RemoteReputationConfig(
        googleSafeBrowsingApiKey = googleSafeBrowsingApiKey.asOptionalApiKey(),
        urlHausApiKey = urlHausApiKey.asOptionalApiKey()
    )
}

private fun String?.asOptionalApiKey(): String? {
    return this?.takeIf { it.isNotBlank() }
}

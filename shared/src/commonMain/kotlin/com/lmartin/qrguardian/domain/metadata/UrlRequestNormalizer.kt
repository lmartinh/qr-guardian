package com.lmartin.qrguardian.domain.metadata

fun normalizeUrlForRequest(url: String): String {
    val trimmed = url.trim()
    if (trimmed.isEmpty()) {
        return trimmed
    }

    if (trimmed.contains("://")) {
        return trimmed
    }

    return "https://$trimmed"
}

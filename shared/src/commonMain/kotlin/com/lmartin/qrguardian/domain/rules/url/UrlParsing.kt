package com.lmartin.qrguardian.domain.rules.url

internal data class ParsedUrl(
    val originalValue: String,
    val scheme: String?,
    val host: String,
    val path: String,
    val query: String,
    val fragment: String?,
)

internal fun parseUrl(value: String): ParsedUrl {
    val trimmed = value.trim()
    val schemeSeparatorIndex = trimmed.indexOf("://")
    val scheme = if (schemeSeparatorIndex >= 0) {
        trimmed.substring(0, schemeSeparatorIndex).lowercase()
    } else {
        null
    }

    val afterScheme = if (schemeSeparatorIndex >= 0) {
        trimmed.substring(schemeSeparatorIndex + 3)
    } else {
        trimmed
    }

    val fragmentSplit = afterScheme.split("#", limit = 2)
    val withoutFragment = fragmentSplit[0]
    val fragment = fragmentSplit.getOrNull(1)

    val querySplit = withoutFragment.split("?", limit = 2)
    val withoutQuery = querySplit[0]
    val query = querySplit.getOrNull(1).orEmpty()

    val pathStartIndex = withoutQuery.indexOf('/')
    val authority = if (pathStartIndex >= 0) {
        withoutQuery.substring(0, pathStartIndex)
    } else {
        withoutQuery
    }
    val path = if (pathStartIndex >= 0) {
        withoutQuery.substring(pathStartIndex)
    } else {
        ""
    }

    val hostWithPort = authority.substringAfterLast('@')
    val host = hostWithPort.substringBefore(':').lowercase()

    return ParsedUrl(
        originalValue = trimmed,
        scheme = scheme,
        host = host,
        path = path,
        query = query,
        fragment = fragment,
    )
}

internal fun ParsedUrl.isIpv4Host(): Boolean {
    val parts = host.split('.')
    if (parts.size != 4) {
        return false
    }

    return parts.all { part ->
        part.isNotBlank() &&
            part.all { it.isDigit() } &&
            part.toIntOrNull()?.let { it in 0..255 } == true
    }
}

internal fun ParsedUrl.hasTooManyQueryParameters(maxQueryParameters: Int): Boolean {
    if (query.isBlank()) {
        return false
    }

    return query.split('&').count { it.isNotBlank() } > maxQueryParameters
}

internal fun ParsedUrl.hasTooManySubdomains(maxParts: Int): Boolean {
    if (host.isBlank()) {
        return false
    }

    return host.split('.').count { it.isNotBlank() } > maxParts
}

internal fun ParsedUrl.hasDangerousFileExtension(dangerousExtensions: Set<String>): Boolean {
    val lowerCasePath = path.lowercase()
    val lastSegment = lowerCasePath.substringAfterLast('/')
    return dangerousExtensions.any { extension -> lastSegment.endsWith(extension) }
}

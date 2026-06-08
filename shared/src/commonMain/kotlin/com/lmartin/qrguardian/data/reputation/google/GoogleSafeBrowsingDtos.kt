package com.lmartin.qrguardian.data.reputation.google

import kotlinx.serialization.Serializable

@Serializable
data class SafeBrowsingRequestDto(
    val client: SafeBrowsingClientDto,
    val threatInfo: SafeBrowsingThreatInfoDto,
)

@Serializable
data class SafeBrowsingClientDto(
    val clientId: String,
    val clientVersion: String,
)

@Serializable
data class SafeBrowsingThreatInfoDto(
    val threatTypes: List<String>,
    val platformTypes: List<String>,
    val threatEntryTypes: List<String>,
    val threatEntries: List<SafeBrowsingThreatEntryDto>,
)

@Serializable
data class SafeBrowsingThreatEntryDto(
    val url: String,
)

@Serializable
data class SafeBrowsingResponseDto(
    val matches: List<SafeBrowsingMatchDto> = emptyList(),
)

@Serializable
data class SafeBrowsingMatchDto(
    val threatType: String? = null,
    val platformType: String? = null,
    val threatEntryType: String? = null,
    val threat: SafeBrowsingThreatEntryDto? = null,
)

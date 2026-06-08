package com.lmartin.qrguardian.domain.reputation

data class UrlReputationResult(
    val status: UrlReputationStatus,
    val provider: String,
    val categories: List<ThreatCategory>,
    val reasons: List<String>,
)

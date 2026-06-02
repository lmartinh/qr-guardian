package com.lmartin.qrguardian.domain.reputation

enum class UrlReputationStatus {
    Clean,
    Suspicious,
    Malicious,
    Unknown,
    NotConfigured,
    Error
}

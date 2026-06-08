package com.lmartin.qrguardian.domain.reputation

enum class ThreatCategory {
    Malware,
    Phishing,
    SocialEngineering,
    UnwantedSoftware,
    Unknown,
}

fun ThreatCategory.displayName(): String = when (this) {
    ThreatCategory.Malware -> "Malware"
    ThreatCategory.Phishing -> "Phishing"
    ThreatCategory.SocialEngineering -> "Social engineering"
    ThreatCategory.UnwantedSoftware -> "Unwanted software"
    ThreatCategory.Unknown -> "Unknown"
}

package com.lmartin.qrguardian.domain.model

enum class SecurityLevel {
    Safe,
    Suspicious,
    Dangerous,
    Unknown,
    ;

    fun title(): String = when (this) {
        Safe -> "Looks safe"
        Suspicious -> "Be careful"
        Dangerous -> "Potentially dangerous"
        Unknown -> "Unknown content"
    }

    fun description(): String = when (this) {
        Safe -> "No suspicious signals were detected."
        Suspicious -> "This QR code contains signals that should be reviewed before opening."
        Dangerous -> "This QR code contains high-risk signals. Opening it is not recommended."
        Unknown -> "This QR code could not be fully evaluated."
    }
}

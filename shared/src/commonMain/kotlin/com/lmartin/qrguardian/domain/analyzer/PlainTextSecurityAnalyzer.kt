package com.lmartin.qrguardian.domain.analyzer

import com.lmartin.qrguardian.domain.model.LocalSecurityCheck
import com.lmartin.qrguardian.domain.model.SecurityLevel

class PlainTextSecurityAnalyzer {
    fun analyze(text: String): LocalSecurityCheck {
        return LocalSecurityCheck(
            level = SecurityLevel.Unknown,
            reasons = listOf("The QR code contains plain text and cannot be fully evaluated locally.")
        )
    }
}

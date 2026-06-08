package com.lmartin.qrguardian.domain.analyzer

import com.lmartin.qrguardian.domain.model.LocalSecurityCheck
import com.lmartin.qrguardian.domain.model.SecurityLevel

class SensitiveActionAnalyzer {
    fun analyzeEmail(text: String): LocalSecurityCheck {
        val reasons =
            mutableListOf(
                "Mailto links can start an email action and should be reviewed before opening.",
            )
        if (text.contains('?') || text.lowercase().contains("subject=") || text.lowercase().contains("body=")) {
            reasons += "The mailto URI includes predefined content such as a subject or body."
        }

        return LocalSecurityCheck(
            level = SecurityLevel.Suspicious,
            reasons = reasons,
        )
    }

    fun analyzePhone(text: String): LocalSecurityCheck = LocalSecurityCheck(
        level = SecurityLevel.Suspicious,
        reasons = listOf("Telephone links can start a sensitive action and should be confirmed manually."),
    )

    fun analyzeSms(text: String): LocalSecurityCheck {
        val lowerCaseText = text.lowercase()
        val reasons =
            mutableListOf(
                "SMS links can start a sensitive action and should be confirmed manually.",
            )

        if (lowerCaseText.contains("body=")) {
            reasons += "The SMS URI includes a predefined message body."
        }

        val bodyValue =
            lowerCaseText
                .substringAfter("body=", "")
                .substringBefore('&')

        if (bodyValue.contains("http://") || bodyValue.contains("https://") || bodyValue.contains("www.")) {
            reasons += "The SMS body contains a URL."
        }

        return LocalSecurityCheck(
            level = SecurityLevel.Suspicious,
            reasons = reasons,
        )
    }

    fun analyzeCrypto(text: String): LocalSecurityCheck = LocalSecurityCheck(
        level = SecurityLevel.Suspicious,
        reasons = listOf("Crypto payment links can transfer assets and should be reviewed carefully."),
    )

    fun analyzeVCard(text: String): LocalSecurityCheck = LocalSecurityCheck(
        level = SecurityLevel.Unknown,
        reasons = listOf("vCard payloads contain contact data and cannot be fully evaluated locally."),
    )

    fun analyzeGeo(text: String): LocalSecurityCheck = LocalSecurityCheck(
        level = SecurityLevel.Unknown,
        reasons = listOf("Geo payloads contain location data and cannot be fully evaluated locally."),
    )
}

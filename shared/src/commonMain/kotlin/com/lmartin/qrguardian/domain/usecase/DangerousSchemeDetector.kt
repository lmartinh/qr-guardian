package com.lmartin.qrguardian.domain.usecase

internal object DangerousSchemeDetector {
    private val blockedSchemes = listOf("javascript:", "file:", "data:", "intent:")

    fun detect(text: String): String? {
        val lowerCaseText = text.trim().lowercase()
        return blockedSchemes.firstOrNull { lowerCaseText.startsWith(it) }
    }
}

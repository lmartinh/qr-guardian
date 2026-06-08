package com.lmartin.qrguardian.domain.analyzer

import com.lmartin.qrguardian.domain.model.LocalSecurityCheck
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel

class DefaultLocalScanAnalyzer(
    private val urlSecurityAnalyzer: UrlLocalSecurityAnalyzer = UrlLocalSecurityAnalyzer(),
    private val wifiSecurityAnalyzer: WifiLocalSecurityAnalyzer = WifiLocalSecurityAnalyzer(),
    private val sensitiveActionAnalyzer: SensitiveActionAnalyzer = SensitiveActionAnalyzer(),
    private val plainTextSecurityAnalyzer: PlainTextSecurityAnalyzer = PlainTextSecurityAnalyzer(),
) : LocalScanAnalyzer {
    override fun analyze(
        rawText: String,
        normalizedText: String,
        contentType: QrContentType,
    ): ScanSectionResult {
        val normalizationCheck = analyzeNormalization(rawText, normalizedText)
        val contentCheck =
            if (normalizedText.isBlank()) {
                LocalSecurityCheck(
                    level = SecurityLevel.Unknown,
                    reasons = emptyList(),
                )
            } else {
                analyzeContent(contentType, normalizedText)
            }

        val reasons = distinctReasons(normalizationCheck.reasons + contentCheck.reasons)
        val finalLevel =
            resolveFinalLevel(
                contentType = contentType,
                normalizationLevel = normalizationCheck.level,
                contentLevel = contentCheck.level,
            )

        return ScanSectionResult(
            name = "Local Scan",
            level = finalLevel,
            status = ScanStatus.Completed,
            title = finalLevel.title(),
            description = finalLevel.description(),
            reasons = reasons,
        )
    }

    private fun analyzeContent(
        contentType: QrContentType,
        normalizedText: String,
    ): LocalSecurityCheck = when (contentType) {
        QrContentType.Url -> {
            urlSecurityAnalyzer.analyze(normalizedText)
        }

        QrContentType.Email -> {
            sensitiveActionAnalyzer.analyzeEmail(normalizedText)
        }

        QrContentType.Phone -> {
            sensitiveActionAnalyzer.analyzePhone(normalizedText)
        }

        QrContentType.Sms -> {
            sensitiveActionAnalyzer.analyzeSms(normalizedText)
        }

        QrContentType.Wifi -> {
            wifiSecurityAnalyzer.analyze(normalizedText)
        }

        QrContentType.VCard -> {
            sensitiveActionAnalyzer.analyzeVCard(normalizedText)
        }

        QrContentType.Geo -> {
            sensitiveActionAnalyzer.analyzeGeo(normalizedText)
        }

        QrContentType.Crypto -> {
            sensitiveActionAnalyzer.analyzeCrypto(normalizedText)
        }

        QrContentType.PlainText -> {
            plainTextSecurityAnalyzer.analyze(normalizedText)
        }

        QrContentType.Unknown -> {
            LocalSecurityCheck(
                level = SecurityLevel.Unknown,
                reasons = listOf("The QR code could not be fully evaluated locally."),
            )
        }
    }

    private fun analyzeNormalization(
        rawText: String,
        normalizedText: String,
    ): LocalSecurityCheck {
        val reasons = mutableListOf<String>()

        if (normalizedText.isBlank()) {
            reasons += "The scanned text is empty after normalization."
        }

        if (rawText.contains('\u0000')) {
            reasons += "Null characters were removed from the scanned text."
        }

        if (containsSuspiciousControlCharacters(rawText)) {
            reasons += "The scanned text contains suspicious control characters."
        }

        if (normalizedText.length > MAX_REASONABLE_TEXT_LENGTH) {
            reasons += "The scanned text is longer than the local safety threshold."
        }

        val level =
            when {
                normalizedText.isBlank() -> SecurityLevel.Unknown
                reasons.isEmpty() -> SecurityLevel.Safe
                else -> SecurityLevel.Suspicious
            }

        return LocalSecurityCheck(
            level = level,
            reasons = reasons,
        )
    }

    private fun resolveFinalLevel(
        contentType: QrContentType,
        normalizationLevel: SecurityLevel,
        contentLevel: SecurityLevel,
    ): SecurityLevel {
        if (normalizationLevel == SecurityLevel.Dangerous || contentLevel == SecurityLevel.Dangerous) {
            return SecurityLevel.Dangerous
        }

        if (normalizationLevel == SecurityLevel.Suspicious || contentLevel == SecurityLevel.Suspicious) {
            return SecurityLevel.Suspicious
        }

        return if (contentType == QrContentType.Url) {
            SecurityLevel.Safe
        } else {
            SecurityLevel.Unknown
        }
    }

    private fun distinctReasons(reasons: List<String>): List<String> {
        val seenReasons = mutableSetOf<String>()
        return reasons.filter { seenReasons.add(it) }
    }

    private fun containsSuspiciousControlCharacters(text: String): Boolean = text.any { character ->
        (character.code < 32 && character !in setOf('\t', '\n', '\r', '\u0000')) ||
            character.code == 127
    }

    private companion object {
        private const val MAX_REASONABLE_TEXT_LENGTH = 4096
    }
}

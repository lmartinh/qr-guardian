package com.lmartin.qrguardian.domain.classifier

import com.lmartin.qrguardian.domain.model.QrContentType

class DefaultQrContentClassifier : QrContentClassifier {
    override fun classify(text: String): QrContentType {
        val normalized = text.trim()
        if (normalized.isEmpty()) {
            return QrContentType.Unknown
        }

        val lowerCaseText = normalized.lowercase()
        return when {
            lowerCaseText.startsWith("mailto:") -> QrContentType.Email
            lowerCaseText.startsWith("tel:") -> QrContentType.Phone
            lowerCaseText.startsWith("sms:") -> QrContentType.Sms
            lowerCaseText.startsWith("wifi:") -> QrContentType.Wifi
            lowerCaseText.startsWith("begin:vcard") -> QrContentType.VCard
            lowerCaseText.startsWith("geo:") -> QrContentType.Geo
            isCryptoUri(lowerCaseText) -> QrContentType.Crypto
            lowerCaseText.startsWith("http://") || lowerCaseText.startsWith("https://") -> QrContentType.Url
            looksLikeUrlWithoutScheme(normalized) -> QrContentType.Url
            else -> QrContentType.PlainText
        }
    }

    private fun isCryptoUri(lowerCaseText: String): Boolean {
        return lowerCaseText.startsWith("bitcoin:") ||
            lowerCaseText.startsWith("ethereum:") ||
            lowerCaseText.startsWith("litecoin:") ||
            lowerCaseText.startsWith("dogecoin:") ||
            lowerCaseText.startsWith("monero:") ||
            lowerCaseText.startsWith("solana:") ||
            lowerCaseText.startsWith("cardano:") ||
            lowerCaseText.startsWith("ripple:")
    }

    private fun looksLikeUrlWithoutScheme(text: String): Boolean {
        if (text.any { it.isWhitespace() }) {
            return false
        }

        val candidate = text
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')
            .substringBefore(':')

        if (!candidate.contains('.')) {
            return false
        }

        val parts = candidate.split('.')
        if (parts.size < 2) {
            return false
        }

        if (parts.any { it.isBlank() }) {
            return false
        }

        return parts.any { segment -> segment.any { it.isLetter() } }
    }
}

package com.lmartin.qrguardian.domain.normalizer

class DefaultQrTextNormalizer : QrTextNormalizer {
    override fun normalize(rawText: String): String {
        return rawText.replace("\u0000", "").trim()
    }
}

package com.lmartin.qrguardian.domain.normalizer

interface QrTextNormalizer {
    fun normalize(rawText: String): String
}

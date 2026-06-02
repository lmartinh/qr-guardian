package com.lmartin.qrguardian.domain.analyzer

import com.lmartin.qrguardian.domain.model.QrSecurityResult

interface QrSecurityAnalyzer {
    fun analyze(rawText: String): QrSecurityResult
}

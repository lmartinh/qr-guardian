package com.lmartin.qrguardian.domain.analyzer

import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanSectionResult

interface LocalScanAnalyzer {
    fun analyze(
        rawText: String,
        normalizedText: String,
        contentType: QrContentType
    ): ScanSectionResult
}

package com.lmartin.qrguardian.domain.model

data class QrAnalysisResult(
    val originalText: String,
    val normalizedText: String,
    val contentType: QrContentType,
    val overallLevel: SecurityLevel,
    val canOpen: Boolean,
    val localScan: ScanSectionResult,
    val remoteReputation: ScanSectionResult
)

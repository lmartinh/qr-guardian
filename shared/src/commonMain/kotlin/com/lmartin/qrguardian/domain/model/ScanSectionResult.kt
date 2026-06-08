package com.lmartin.qrguardian.domain.model

data class ScanSectionResult(
    val name: String,
    val level: SecurityLevel,
    val status: ScanStatus,
    val title: String,
    val description: String,
    val reasons: List<String>,
    val metadata: List<ScanMetadataItem> = emptyList(),
)

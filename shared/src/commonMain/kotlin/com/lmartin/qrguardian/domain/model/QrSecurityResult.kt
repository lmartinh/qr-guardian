package com.lmartin.qrguardian.domain.model

import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus

data class QrSecurityResult(
    val originalText: String,
    val normalizedText: String,
    val contentType: QrContentType,
    val securityLevel: SecurityLevel,
    val title: String,
    val description: String,
    val reasons: List<String>,
    val canOpen: Boolean,
    val remoteReputationStatus: UrlReputationStatus = UrlReputationStatus.NotConfigured
)

package com.lmartin.qrguardian.domain.model

data class LocalSecurityCheck(
    val level: SecurityLevel,
    val reasons: List<String>,
)

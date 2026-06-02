package com.lmartin.qrguardian.domain.model

data class SecurityRuleResult(
    val triggered: Boolean,
    val level: SecurityLevel,
    val reason: String?
)

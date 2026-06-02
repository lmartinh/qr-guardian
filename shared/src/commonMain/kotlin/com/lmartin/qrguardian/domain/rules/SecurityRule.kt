package com.lmartin.qrguardian.domain.rules

import com.lmartin.qrguardian.domain.model.SecurityRuleResult

interface SecurityRule {
    fun evaluate(value: String): SecurityRuleResult
}

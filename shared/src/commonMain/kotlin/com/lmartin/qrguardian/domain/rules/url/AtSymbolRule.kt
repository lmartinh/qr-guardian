package com.lmartin.qrguardian.domain.rules.url

import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.model.SecurityRuleResult
import com.lmartin.qrguardian.domain.rules.SecurityRule

class AtSymbolRule : SecurityRule {
    override fun evaluate(value: String): SecurityRuleResult {
        val triggered = value.contains('@')
        return SecurityRuleResult(
            triggered = triggered,
            level = if (triggered) SecurityLevel.Suspicious else SecurityLevel.Safe,
            reason = if (triggered) "The URL contains an @ symbol, which can hide the real destination." else null,
        )
    }
}

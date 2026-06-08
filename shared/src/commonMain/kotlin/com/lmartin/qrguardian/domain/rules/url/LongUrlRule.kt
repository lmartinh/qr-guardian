package com.lmartin.qrguardian.domain.rules.url

import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.model.SecurityRuleResult
import com.lmartin.qrguardian.domain.rules.SecurityRule

class LongUrlRule(
    private val maxLength: Int = 300,
) : SecurityRule {
    override fun evaluate(value: String): SecurityRuleResult {
        val triggered = value.length > maxLength
        return SecurityRuleResult(
            triggered = triggered,
            level = if (triggered) SecurityLevel.Suspicious else SecurityLevel.Safe,
            reason = if (triggered) "The URL is longer than the local safety threshold." else null,
        )
    }
}

package com.lmartin.qrguardian.domain.rules.url

import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.model.SecurityRuleResult
import com.lmartin.qrguardian.domain.rules.SecurityRule

class HttpsRule : SecurityRule {
    override fun evaluate(value: String): SecurityRuleResult {
        val triggered = !value.trim().lowercase().startsWith("https://")
        return SecurityRuleResult(
            triggered = triggered,
            level = if (triggered) SecurityLevel.Suspicious else SecurityLevel.Safe,
            reason = if (triggered) "The URL does not use HTTPS." else null
        )
    }
}

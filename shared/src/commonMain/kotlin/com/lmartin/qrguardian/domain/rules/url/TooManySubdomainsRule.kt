package com.lmartin.qrguardian.domain.rules.url

import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.model.SecurityRuleResult
import com.lmartin.qrguardian.domain.rules.SecurityRule

class TooManySubdomainsRule(
    private val maxHostParts: Int = 4
) : SecurityRule {
    override fun evaluate(value: String): SecurityRuleResult {
        val parsedUrl = parseUrl(value)
        val triggered = parsedUrl.hasTooManySubdomains(maxHostParts)
        return SecurityRuleResult(
            triggered = triggered,
            level = if (triggered) SecurityLevel.Suspicious else SecurityLevel.Safe,
            reason = if (triggered) "The host contains too many subdomains." else null
        )
    }
}

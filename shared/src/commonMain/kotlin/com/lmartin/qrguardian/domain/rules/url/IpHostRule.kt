package com.lmartin.qrguardian.domain.rules.url

import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.model.SecurityRuleResult
import com.lmartin.qrguardian.domain.rules.SecurityRule

class IpHostRule : SecurityRule {
    override fun evaluate(value: String): SecurityRuleResult {
        val parsedUrl = parseUrl(value)
        val triggered = parsedUrl.isIpv4Host()
        return SecurityRuleResult(
            triggered = triggered,
            level = if (triggered) SecurityLevel.Suspicious else SecurityLevel.Safe,
            reason = if (triggered) "The URL uses an IPv4 address as host." else null,
        )
    }
}

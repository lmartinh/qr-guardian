package com.lmartin.qrguardian.domain.rules.url

import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.model.SecurityRuleResult
import com.lmartin.qrguardian.domain.rules.SecurityRule

class QueryParamsRule(
    private val maxQueryParameters: Int = 8
) : SecurityRule {
    override fun evaluate(value: String): SecurityRuleResult {
        val parsedUrl = parseUrl(value)
        val triggered = parsedUrl.hasTooManyQueryParameters(maxQueryParameters)
        return SecurityRuleResult(
            triggered = triggered,
            level = if (triggered) SecurityLevel.Suspicious else SecurityLevel.Safe,
            reason = if (triggered) "The URL has too many query parameters." else null
        )
    }
}

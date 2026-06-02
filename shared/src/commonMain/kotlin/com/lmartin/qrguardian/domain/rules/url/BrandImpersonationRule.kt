package com.lmartin.qrguardian.domain.rules.url

import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.model.SecurityRuleResult
import com.lmartin.qrguardian.domain.rules.SecurityRule

class BrandImpersonationRule : SecurityRule {
    private val brands = setOf(
        "google",
        "paypal",
        "microsoft",
        "apple",
        "amazon",
        "facebook",
        "instagram",
        "whatsapp",
        "bbva",
        "santander",
        "caixabank"
    )

    private val contextWords = setOf(
        "login",
        "verify",
        "secure",
        "account",
        "support",
        "update"
    )

    override fun evaluate(value: String): SecurityRuleResult {
        val parsedUrl = parseUrl(value)
        val host = parsedUrl.host
        val triggered = brands.any { brand ->
            host.contains(brand) && contextWords.any { contextWord -> host.contains(contextWord) }
        }
        return SecurityRuleResult(
            triggered = triggered,
            level = if (triggered) SecurityLevel.Suspicious else SecurityLevel.Safe,
            reason = if (triggered) "The host combines a known brand with security-related wording." else null
        )
    }
}

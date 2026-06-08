package com.lmartin.qrguardian.domain.rules.url

import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.model.SecurityRuleResult
import com.lmartin.qrguardian.domain.rules.SecurityRule

class LinkShortenerRule : SecurityRule {
    private val shortenerHosts =
        setOf(
            "bit.ly",
            "tinyurl.com",
            "t.co",
            "goo.gl",
            "ow.ly",
            "is.gd",
            "buff.ly",
            "cutt.ly",
            "shorturl.at",
            "rebrand.ly",
        )

    override fun evaluate(value: String): SecurityRuleResult {
        val parsedUrl = parseUrl(value)
        val triggered = parsedUrl.host in shortenerHosts
        return SecurityRuleResult(
            triggered = triggered,
            level = if (triggered) SecurityLevel.Suspicious else SecurityLevel.Safe,
            reason = if (triggered) "The URL uses a known link shortener." else null,
        )
    }
}

package com.lmartin.qrguardian.domain.rules.url

import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.model.SecurityRuleResult
import com.lmartin.qrguardian.domain.rules.SecurityRule

class SuspiciousWordsRule : SecurityRule {
    private val suspiciousWords =
        setOf(
            "login",
            "verify",
            "verification",
            "password",
            "passwd",
            "bank",
            "wallet",
            "crypto",
            "security",
            "account",
            "update",
            "confirm",
            "free",
            "gift",
            "prize",
            "urgent",
        )

    override fun evaluate(value: String): SecurityRuleResult {
        val lowerCaseValue = value.lowercase()
        val triggered = suspiciousWords.any { lowerCaseValue.contains(it) }
        return SecurityRuleResult(
            triggered = triggered,
            level = if (triggered) SecurityLevel.Suspicious else SecurityLevel.Safe,
            reason = if (triggered) "The URL contains sensitive wording that deserves a manual review." else null,
        )
    }
}

package com.lmartin.qrguardian.domain.analyzer

import com.lmartin.qrguardian.domain.model.LocalSecurityCheck
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.rules.SecurityRule
import com.lmartin.qrguardian.domain.rules.url.AtSymbolRule
import com.lmartin.qrguardian.domain.rules.url.BrandImpersonationRule
import com.lmartin.qrguardian.domain.rules.url.DangerousFileExtensionRule
import com.lmartin.qrguardian.domain.rules.url.HttpsRule
import com.lmartin.qrguardian.domain.rules.url.IpHostRule
import com.lmartin.qrguardian.domain.rules.url.LinkShortenerRule
import com.lmartin.qrguardian.domain.rules.url.LongUrlRule
import com.lmartin.qrguardian.domain.rules.url.QueryParamsRule
import com.lmartin.qrguardian.domain.rules.url.SuspiciousWordsRule
import com.lmartin.qrguardian.domain.rules.url.TooManySubdomainsRule

class UrlLocalSecurityAnalyzer(
    private val rules: List<SecurityRule> =
        listOf(
            HttpsRule(),
            AtSymbolRule(),
            IpHostRule(),
            LinkShortenerRule(),
            DangerousFileExtensionRule(),
            SuspiciousWordsRule(),
            LongUrlRule(),
            QueryParamsRule(),
            TooManySubdomainsRule(),
            BrandImpersonationRule(),
        ),
) {
    fun analyze(url: String): LocalSecurityCheck {
        val triggeredResults = rules.map { it.evaluate(url) }.filter { it.triggered }
        if (triggeredResults.isEmpty()) {
            return LocalSecurityCheck(
                level = SecurityLevel.Safe,
                reasons = emptyList(),
            )
        }

        val level =
            when {
                triggeredResults.any { it.level == SecurityLevel.Dangerous } -> SecurityLevel.Dangerous
                triggeredResults.any { it.level == SecurityLevel.Suspicious } -> SecurityLevel.Suspicious
                else -> SecurityLevel.Safe
            }

        return LocalSecurityCheck(
            level = level,
            reasons = triggeredResults.mapNotNull { it.reason },
        )
    }
}

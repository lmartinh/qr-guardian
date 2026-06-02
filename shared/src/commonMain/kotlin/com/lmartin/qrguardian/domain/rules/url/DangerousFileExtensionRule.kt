package com.lmartin.qrguardian.domain.rules.url

import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.model.SecurityRuleResult
import com.lmartin.qrguardian.domain.rules.SecurityRule

class DangerousFileExtensionRule : SecurityRule {
    private val dangerousExtensions = setOf(
        ".apk",
        ".exe",
        ".scr",
        ".bat",
        ".cmd",
        ".js",
        ".jar",
        ".msi",
        ".dmg",
        ".pkg"
    )

    override fun evaluate(value: String): SecurityRuleResult {
        val parsedUrl = parseUrl(value)
        val triggered = parsedUrl.hasDangerousFileExtension(dangerousExtensions)
        return SecurityRuleResult(
            triggered = triggered,
            level = if (triggered) SecurityLevel.Dangerous else SecurityLevel.Safe,
            reason = if (triggered) "The URL points to a potentially dangerous file type." else null
        )
    }
}

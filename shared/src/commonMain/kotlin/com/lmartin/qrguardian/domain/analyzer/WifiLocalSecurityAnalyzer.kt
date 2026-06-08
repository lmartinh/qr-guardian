package com.lmartin.qrguardian.domain.analyzer

import com.lmartin.qrguardian.domain.model.LocalSecurityCheck
import com.lmartin.qrguardian.domain.model.SecurityLevel

class WifiLocalSecurityAnalyzer {
    fun analyze(wifiText: String): LocalSecurityCheck {
        val fields = parseFields(wifiText)
        val reasons =
            mutableListOf(
                "WiFi QR codes configure a network, which is a sensitive action.",
            )

        val securityType = fields["T"]?.lowercase()
        if (securityType == "nopass") {
            reasons += "The WiFi QR code declares an open network (T:nopass)."
        }

        if (!fields.containsKey("S")) {
            reasons += "The WiFi QR code is missing the network name (S:)."
        }

        return LocalSecurityCheck(
            level = SecurityLevel.Suspicious,
            reasons = reasons,
        )
    }

    private fun parseFields(wifiText: String): Map<String, String> {
        val payload = wifiText.removePrefix("WIFI:")
        val result = mutableMapOf<String, String>()

        payload
            .split(';')
            .asSequence()
            .filter { it.isNotBlank() }
            .forEach { entry ->
                val separatorIndex = entry.indexOf(':')
                if (separatorIndex > 0) {
                    val key = entry.substring(0, separatorIndex).uppercase()
                    val value = entry.substring(separatorIndex + 1)
                    result[key] = value
                }
            }

        return result
    }
}

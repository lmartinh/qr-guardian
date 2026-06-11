package com.lmartin.qrguardian.domain.reputation

import kotlin.test.Test
import kotlin.test.assertEquals

class ThreatCategoryTest {
    @Test
    fun `display names are stable`() {
        val cases =
            mapOf(
                ThreatCategory.Malware to "Malware",
                ThreatCategory.Phishing to "Phishing",
                ThreatCategory.SocialEngineering to "Social engineering",
                ThreatCategory.UnwantedSoftware to "Unwanted software",
                ThreatCategory.Unknown to "Unknown",
            )

        cases.forEach { (category, expected) ->
            assertEquals(expected, category.displayName(), category.name)
        }
    }
}

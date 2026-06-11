package com.lmartin.qrguardian.presentation.result

import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.presentation.theme.QrGuardianColors
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultToneTest {
    @Test
    fun `security levels map to stable tones and icons`() {
        val safeTone = SecurityLevel.Safe.toResultTone()
        val suspiciousTone = SecurityLevel.Suspicious.toResultTone()
        val dangerousTone = SecurityLevel.Dangerous.toResultTone()
        val unknownTone = SecurityLevel.Unknown.toResultTone()

        assertEquals(SecurityLevel.Safe.toStatusIcon(), safeTone.icon)
        assertEquals(SecurityLevel.Suspicious.toStatusIcon(), suspiciousTone.icon)
        assertEquals(SecurityLevel.Dangerous.toStatusIcon(), dangerousTone.icon)
        assertEquals(SecurityLevel.Unknown.toStatusIcon(), unknownTone.icon)

        assertEquals(QrGuardianColors.SafeContainerLight, safeTone.sectionTint(SecurityLevel.Safe))
        assertEquals(QrGuardianColors.WarningContainerLight, suspiciousTone.sectionTint(SecurityLevel.Suspicious))
        assertEquals(QrGuardianColors.DangerContainerLight, dangerousTone.sectionTint(SecurityLevel.Dangerous))
        assertEquals(QrGuardianColors.Secondary.copy(alpha = 0.16f), unknownTone.sectionTint(SecurityLevel.Unknown))

        assertEquals(QrGuardianColors.Safe, safeTone.sectionContent(SecurityLevel.Safe))
        assertEquals(androidx.compose.ui.graphics.Color(0xFF9A6B00), suspiciousTone.sectionContent(SecurityLevel.Suspicious))
        assertEquals(androidx.compose.ui.graphics.Color(0xFFB42318), dangerousTone.sectionContent(SecurityLevel.Dangerous))
        assertEquals(QrGuardianColors.PrimaryDark, unknownTone.sectionContent(SecurityLevel.Unknown))
    }
}

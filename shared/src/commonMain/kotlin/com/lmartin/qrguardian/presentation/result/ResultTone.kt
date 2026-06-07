package com.lmartin.qrguardian.presentation.result

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.presentation.theme.QrGuardianColors

internal data class ResultTone(
    val icon: ImageVector,
    val badgeContainerColor: Color,
    val badgeContentColor: Color,
    val accentColor: Color,
    val accentContentColor: Color,
    val pillContainerColor: Color,
    val pillContentColor: Color,
    val actionContainerColor: Color,
    val actionContentColor: Color,
)

internal fun SecurityLevel.toResultTone(): ResultTone {
    return when (this) {
        SecurityLevel.Safe -> ResultTone(
            icon = Icons.Filled.CheckCircle,
            badgeContainerColor = QrGuardianColors.SafeContainerLight,
            badgeContentColor = QrGuardianColors.Safe,
            accentColor = QrGuardianColors.Safe,
            accentContentColor = QrGuardianColors.SafeContainerLight,
            pillContainerColor = QrGuardianColors.SafeContainerLight,
            pillContentColor = QrGuardianColors.Safe,
            actionContainerColor = QrGuardianColors.PrimaryDark,
            actionContentColor = Color.White,
        )
        SecurityLevel.Suspicious -> ResultTone(
            icon = Icons.Filled.Warning,
            badgeContainerColor = QrGuardianColors.WarningContainerLight,
            badgeContentColor = Color(0xFF9A6B00),
            accentColor = QrGuardianColors.Warning,
            accentContentColor = QrGuardianColors.WarningContainerLight,
            pillContainerColor = QrGuardianColors.WarningContainerLight,
            pillContentColor = Color(0xFF9A6B00),
            actionContainerColor = QrGuardianColors.PrimaryDark.copy(alpha = 0.92f),
            actionContentColor = Color.White,
        )
        SecurityLevel.Dangerous -> ResultTone(
            icon = Icons.Filled.Error,
            badgeContainerColor = QrGuardianColors.DangerContainerLight,
            badgeContentColor = Color(0xFFB42318),
            accentColor = QrGuardianColors.Danger,
            accentContentColor = QrGuardianColors.DangerContainerLight,
            pillContainerColor = QrGuardianColors.DangerContainerLight,
            pillContentColor = Color(0xFFB42318),
            actionContainerColor = QrGuardianColors.Danger.copy(alpha = 0.20f),
            actionContentColor = Color(0xFFB42318),
        )
        SecurityLevel.Unknown -> ResultTone(
            icon = Icons.Filled.Warning,
            badgeContainerColor = QrGuardianColors.Secondary.copy(alpha = 0.24f),
            badgeContentColor = QrGuardianColors.PrimaryDark,
            accentColor = QrGuardianColors.Neutral,
            accentContentColor = QrGuardianColors.Secondary,
            pillContainerColor = QrGuardianColors.Secondary.copy(alpha = 0.18f),
            pillContentColor = QrGuardianColors.PrimaryDark,
            actionContainerColor = QrGuardianColors.PrimaryDark.copy(alpha = 0.88f),
            actionContentColor = Color.White,
        )
    }
}

internal fun ResultTone.sectionTint(level: SecurityLevel): Color {
    return when (level) {
        SecurityLevel.Safe -> QrGuardianColors.SafeContainerLight
        SecurityLevel.Suspicious -> QrGuardianColors.WarningContainerLight
        SecurityLevel.Dangerous -> QrGuardianColors.DangerContainerLight
        SecurityLevel.Unknown -> QrGuardianColors.Secondary.copy(alpha = 0.16f)
    }
}

internal fun ResultTone.sectionContent(level: SecurityLevel): Color {
    return when (level) {
        SecurityLevel.Safe -> QrGuardianColors.Safe
        SecurityLevel.Suspicious -> Color(0xFF9A6B00)
        SecurityLevel.Dangerous -> Color(0xFFB42318)
        SecurityLevel.Unknown -> QrGuardianColors.PrimaryDark
    }
}

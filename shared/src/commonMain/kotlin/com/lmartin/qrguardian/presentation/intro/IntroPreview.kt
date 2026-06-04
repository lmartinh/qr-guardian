package com.lmartin.qrguardian.presentation.intro

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lmartin.qrguardian.presentation.theme.QrGuardianTheme

@Preview(name = "Default intro", showBackground = true)
@Composable
private fun IntroScreenPreviewDefault() {
    QrGuardianTheme {
        IntroScreen(
            onStartScanningClick = {},
            showPermissionMessage = false,
            showPermissionSettingsCard = false,
            onOpenSettingsClick = {},
        )
    }
}

@Preview(name = "Permission blocked", showBackground = true)
@Composable
private fun IntroScreenPreviewPermissionBlocked() {
    QrGuardianTheme {
        IntroScreen(
            onStartScanningClick = {},
            showPermissionMessage = true,
            showPermissionSettingsCard = true,
            onOpenSettingsClick = {},
        )
    }
}

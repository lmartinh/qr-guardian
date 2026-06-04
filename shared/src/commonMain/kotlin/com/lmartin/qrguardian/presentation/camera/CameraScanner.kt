package com.lmartin.qrguardian.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformQrScanner(
    isActive: Boolean,
    isTorchEnabled: Boolean,
    onScanResult: (String) -> Unit,
    onScannerError: (String) -> Unit,
    onTorchAvailabilityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
)

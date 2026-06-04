package com.lmartin.qrguardian.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lmartin.qrguardian.presentation.theme.QrGuardianTheme

@Preview
@Composable
private fun CameraScreenPreview() {
    QrGuardianTheme {
        CameraScreen(
            viewModel = CameraViewModel.preview(
                CameraUiState(
                    isScanning = true,
                    isTorchAvailable = true,
                    isTorchEnabled = false,
                )
            ),
            onCloseClick = {},
            onScanResult = {},
        )
    }
}

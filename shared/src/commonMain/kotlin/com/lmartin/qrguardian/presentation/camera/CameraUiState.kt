package com.lmartin.qrguardian.presentation.camera

data class CameraUiState(
    val isScanning: Boolean = true,
    val isTorchAvailable: Boolean = false,
    val isTorchEnabled: Boolean = false,
    val errorMessage: String? = null,
) {
    companion object {
        fun idle() = CameraUiState()
    }
}

package com.lmartin.qrguardian.presentation.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CameraViewModel(
    initialState: CameraUiState = CameraUiState.idle(),
) {
    var uiState by mutableStateOf(initialState)
        private set

    fun setTorchAvailable(isAvailable: Boolean) {
        uiState = uiState.copy(isTorchAvailable = isAvailable)
    }

    fun toggleTorch() {
        if (!uiState.isTorchAvailable) return
        uiState = uiState.copy(isTorchEnabled = !uiState.isTorchEnabled)
    }

    fun setScanning(isScanning: Boolean) {
        uiState = uiState.copy(isScanning = isScanning)
    }

    fun setError(message: String?) {
        uiState = uiState.copy(errorMessage = message)
    }

    companion object {
        fun preview(state: CameraUiState = CameraUiState.idle()): CameraViewModel = CameraViewModel(state)
    }
}

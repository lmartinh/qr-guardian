package com.lmartin.qrguardian.presentation.camera

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CameraViewModelTest {
    @Test
    fun `torch toggles only when available`() {
        val viewModel = CameraViewModel(initialState = CameraUiState.idle())

        assertFalse(viewModel.uiState.isTorchEnabled)

        viewModel.toggleTorch()

        assertFalse(viewModel.uiState.isTorchEnabled)

        viewModel.setTorchAvailable(true)
        viewModel.toggleTorch()

        assertTrue(viewModel.uiState.isTorchEnabled)
    }

    @Test
    fun `scanner and error state updates are reflected in ui state`() {
        val viewModel = CameraViewModel(initialState = CameraUiState.idle())

        viewModel.setScanning(false)
        viewModel.setError("Camera unavailable")

        assertFalse(viewModel.uiState.isScanning)
        assertTrue(viewModel.uiState.errorMessage == "Camera unavailable")
    }
}

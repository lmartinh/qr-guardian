package com.lmartin.qrguardian.presentation.permissions

data class CameraPermissionState(
    val isGranted: Boolean,
    val canRequestAgain: Boolean,
) {
    companion object {
        fun granted(): CameraPermissionState = CameraPermissionState(
            isGranted = true,
            canRequestAgain = true,
        )

        fun denied(canRequestAgain: Boolean): CameraPermissionState = CameraPermissionState(
            isGranted = false,
            canRequestAgain = canRequestAgain,
        )
    }
}

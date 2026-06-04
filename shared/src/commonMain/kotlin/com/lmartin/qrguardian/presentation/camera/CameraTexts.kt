package com.lmartin.qrguardian.presentation.camera

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import qrguardian.shared.generated.resources.Res
import qrguardian.shared.generated.resources.camera_close
import qrguardian.shared.generated.resources.camera_hint
import qrguardian.shared.generated.resources.camera_scanner_frame
import qrguardian.shared.generated.resources.camera_scanner_placeholder
import qrguardian.shared.generated.resources.camera_scanning
import qrguardian.shared.generated.resources.camera_subtitle
import qrguardian.shared.generated.resources.camera_title
import qrguardian.shared.generated.resources.camera_torch_off
import qrguardian.shared.generated.resources.camera_torch_on

@Composable
internal fun rememberCameraTexts(): CameraTexts {
    return CameraTexts(
        title = stringResource(Res.string.camera_title),
        subtitle = stringResource(Res.string.camera_subtitle),
        hint = stringResource(Res.string.camera_hint),
        torchOn = stringResource(Res.string.camera_torch_on),
        torchOff = stringResource(Res.string.camera_torch_off),
        close = stringResource(Res.string.camera_close),
        scanning = stringResource(Res.string.camera_scanning),
        scannerPlaceholder = stringResource(Res.string.camera_scanner_placeholder),
        scannerFrame = stringResource(Res.string.camera_scanner_frame),
    )
}

internal data class CameraTexts(
    val title: String,
    val subtitle: String,
    val hint: String,
    val torchOn: String,
    val torchOff: String,
    val close: String,
    val scanning: String,
    val scannerPlaceholder: String,
    val scannerFrame: String,
)

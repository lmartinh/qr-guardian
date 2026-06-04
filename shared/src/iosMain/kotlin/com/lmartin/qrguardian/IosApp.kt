package com.lmartin.qrguardian

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.lmartin.qrguardian.core.network.QrGuardianHttpClientFactory
import com.lmartin.qrguardian.core.security.QrGuardianSecurityPipelineFactory
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.launch
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

@Composable
fun IosApp() {
    val scope = rememberCoroutineScope()
    var cameraPermissionGranted by remember {
        mutableStateOf(hasCameraPermission())
    }

    val httpClient = remember {
        QrGuardianHttpClientFactory.create(Darwin)
    }
    val analyzeQr = remember(httpClient) {
        QrGuardianSecurityPipelineFactory.createAnalyzeQrSafetyUseCase(httpClient)::invoke
    }

    DisposableEffect(Unit) {
        onDispose {
            httpClient.close()
        }
    }

    App(
        cameraPermissionGranted = cameraPermissionGranted,
        onRequestCameraPermission = {
            requestCameraPermission { granted ->
                scope.launch {
                    cameraPermissionGranted = granted
                }
            }
        },
        analyzeQr = analyzeQr,
    )
}

private fun hasCameraPermission(): Boolean {
    return when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
        AVAuthorizationStatusAuthorized -> true
        AVAuthorizationStatusDenied,
        AVAuthorizationStatusRestricted,
        AVAuthorizationStatusNotDetermined -> false
        else -> false
    }
}

private fun requestCameraPermission(onResult: (Boolean) -> Unit) {
    when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
        AVAuthorizationStatusAuthorized -> onResult(true)
        AVAuthorizationStatusDenied,
        AVAuthorizationStatusRestricted -> onResult(false)
        AVAuthorizationStatusNotDetermined -> {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                onResult(granted)
            }
        }
        else -> onResult(false)
    }
}

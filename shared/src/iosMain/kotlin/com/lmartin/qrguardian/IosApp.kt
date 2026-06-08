package com.lmartin.qrguardian

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.lmartin.qrguardian.data.reputation.IosRemoteReputationConfigProvider
import com.lmartin.qrguardian.di.initKoin
import com.lmartin.qrguardian.domain.usecase.AnalyzeQrSafetyUseCase
import com.lmartin.qrguardian.presentation.permissions.CameraPermissionState
import kotlinx.coroutines.launch
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationOpenSettingsURLString

@Composable
fun IosApp() {
    val scope = rememberCoroutineScope()
    var cameraPermissionState by remember {
        mutableStateOf(resolveCameraPermissionState())
    }

    val remoteReputationConfig = remember {
        IosRemoteReputationConfigProvider.create()
    }
    val koinApplication = remember(remoteReputationConfig) {
        initKoin(remoteReputationConfig)
    }
    val analyzeQrSafetyUseCase = remember(koinApplication) {
        koinApplication.koin.get<AnalyzeQrSafetyUseCase>()
    }
    val analyzeQr = remember(analyzeQrSafetyUseCase) {
        analyzeQrSafetyUseCase::invoke
    }

    DisposableEffect(koinApplication) {
        onDispose {
            koinApplication.close()
        }
    }

    DisposableEffect(Unit) {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null,
            usingBlock = {
                cameraPermissionState = resolveCameraPermissionState()
            },
        )
        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }

    App(
        cameraPermissionState = cameraPermissionState,
        onRequestCameraPermission = { onResult ->
            requestCameraPermission { granted ->
                scope.launch {
                    cameraPermissionState = resolveCameraPermissionState()
                    onResult(granted)
                }
            }
        },
        onOpenCameraSettings = {
            val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
            if (settingsUrl != null) {
                UIApplication.sharedApplication.openURL(settingsUrl)
            }
        },
        analyzeQr = analyzeQr,
    )
}

private fun resolveCameraPermissionState(): CameraPermissionState = when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
    AVAuthorizationStatusAuthorized -> CameraPermissionState.granted()

    AVAuthorizationStatusDenied,
    AVAuthorizationStatusRestricted,
    -> CameraPermissionState.denied(canRequestAgain = false)

    AVAuthorizationStatusNotDetermined -> CameraPermissionState.denied(canRequestAgain = true)

    else -> CameraPermissionState.denied(canRequestAgain = false)
}

private fun requestCameraPermission(onResult: (Boolean) -> Unit) {
    when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
        AVAuthorizationStatusAuthorized -> onResult(true)

        AVAuthorizationStatusDenied,
        AVAuthorizationStatusRestricted,
        -> onResult(false)

        AVAuthorizationStatusNotDetermined -> {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                onResult(granted)
            }
        }

        else -> onResult(false)
    }
}

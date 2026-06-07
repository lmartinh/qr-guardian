package com.lmartin.qrguardian

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanMetadataItem
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.presentation.app.AppScreen
import com.lmartin.qrguardian.presentation.app.rememberQrGuardianAppState
import com.lmartin.qrguardian.presentation.camera.CameraScreen
import com.lmartin.qrguardian.presentation.camera.CameraViewModel
import com.lmartin.qrguardian.presentation.intro.IntroScreen
import com.lmartin.qrguardian.presentation.permissions.CameraPermissionState
import com.lmartin.qrguardian.presentation.result.ResultScreen
import com.lmartin.qrguardian.presentation.result.ResultUiState
import com.lmartin.qrguardian.presentation.result.ResultViewModel
import com.lmartin.qrguardian.presentation.theme.QrGuardianTheme

@Composable
@Preview
fun App() {
    App(analyzeQr = { demoResult() })
}

@Composable
fun App(
    cameraPermissionState: CameraPermissionState = CameraPermissionState.granted(),
    onRequestCameraPermission: ((Boolean) -> Unit) -> Unit = {},
    onOpenCameraSettings: () -> Unit = {},
    analyzeQr: suspend (String) -> QrAnalysisResult,
) {
    val uriHandler = LocalUriHandler.current
    val appState = rememberQrGuardianAppState(initialCameraPermissionGranted = cameraPermissionState.isGranted)
    val cameraViewModel = remember { CameraViewModel() }
    val resultViewModel = remember { ResultViewModel(ResultUiState.idle()) }

    LaunchedEffect(appState.screen) {
        if (appState.screen == AppScreen.Camera) {
            cameraViewModel.setScanning(true)
            cameraViewModel.setError(null)
        } else {
            cameraViewModel.setScanning(false)
        }
    }

    LaunchedEffect(cameraPermissionState.isGranted, appState.waitingForSettingsReturn, appState.screen) {
        appState.syncCameraPermissionState(cameraPermissionState.isGranted)
        when {
            cameraPermissionState.isGranted && appState.waitingForSettingsReturn -> {
                appState.onCameraPermissionGranted()
            }

            !cameraPermissionState.isGranted && appState.screen == AppScreen.Camera -> {
                appState.onBackToIntro()
            }
        }
    }

    LaunchedEffect(appState.screen, appState.pendingAnalysisRawText) {
        val pendingRawText = appState.pendingAnalysisRawText
        if (appState.screen == AppScreen.Result && pendingRawText != null) {
            resultViewModel.showLoading()
            runCatching { analyzeQr(pendingRawText) }
                .onSuccess {
                    resultViewModel.showResult(it)
                    appState.onAnalysisResult()
                }
                .onFailure { throwable ->
                    resultViewModel.showError(
                        throwable.message ?: "Unable to analyze the scanned content."
                    )
                    appState.onAnalysisError()
                }
        }
    }

    QrGuardianTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
        ) {
            when (appState.screen) {
                AppScreen.Intro -> IntroScreen(
                    onStartScanningClick = {
                        appState.onStartScan()
                        when {
                            cameraPermissionState.isGranted -> {
                                appState.onCameraPermissionGranted()
                            }

                            cameraPermissionState.canRequestAgain -> {
                                onRequestCameraPermission { granted ->
                                    if (granted) {
                                        appState.onCameraPermissionGranted()
                                    } else {
                                        appState.onCameraPermissionDenied()
                                    }
                                }
                            }

                            else -> {
                                appState.onBackToIntro()
                            }
                        }
                    },
                    showPermissionMessage = appState.showPermissionMessage,
                    showPermissionSettingsCard = !cameraPermissionState.isGranted && !cameraPermissionState.canRequestAgain,
                    onOpenSettingsClick = {
                        appState.onOpenCameraSettings()
                        onOpenCameraSettings()
                    },
                )

                AppScreen.Camera -> CameraScreen(
                    viewModel = cameraViewModel,
                    onCloseClick = {
                        resultViewModel.reset()
                        cameraViewModel.setScanning(false)
                        cameraViewModel.setError(null)
                        appState.onCloseCamera()
                    },
                    onScanResult = { rawText ->
                        if (!appState.onQrDetected(rawText)) return@CameraScreen
                        resultViewModel.showLoading()
                    },
                )

                AppScreen.Result -> ResultScreen(
                    viewModel = resultViewModel,
                    onOpenUrl = { uriHandler.openUri(it) },
                    onRescanClick = {
                        resultViewModel.reset()
                        appState.onRescan()
                    },
                )
            }
        }
    }
}

private fun demoResult(): QrAnalysisResult {
    return QrAnalysisResult(
        originalText = "https://secure-login.example.com/account",
        normalizedText = "https://secure-login.example.com/account",
        openableUrl = "https://secure-login.example.com/account",
        contentType = QrContentType.Url,
        overallLevel = SecurityLevel.Suspicious,
        canOpen = true,
        localScan = ScanSectionResult(
            name = "Local scan",
            level = SecurityLevel.Suspicious,
            status = ScanStatus.Completed,
            title = "Phishing-like patterns detected",
            description = "The destination looks like a login or credential collection flow.",
            reasons = listOf(
                "The domain mirrors a login pattern.",
                "The path suggests a sign-in flow.",
                "The destination asks for user action.",
            ),
            metadata = listOf(
                ScanMetadataItem(label = "Content", value = "URL"),
            ),
        ),
        remoteReputation = ScanSectionResult(
            name = "Remote reputation",
            level = SecurityLevel.Unknown,
            status = ScanStatus.Unavailable,
            title = "Reputation not available",
            description = "Remote checks are not available in this demo state.",
            reasons = listOf(
                "No backend reputation provider is configured.",
                "The app keeps the result visible before opening the link.",
            ),
            metadata = listOf(
                ScanMetadataItem(label = "Provider", value = "Not configured"),
                ScanMetadataItem(label = "Last check", value = "Unavailable"),
            ),
        ),
    )
}

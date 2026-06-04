package com.lmartin.qrguardian

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalUriHandler
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.presentation.permissions.CameraPermissionState
import com.lmartin.qrguardian.presentation.intro.IntroScreen
import com.lmartin.qrguardian.presentation.camera.CameraScreen
import com.lmartin.qrguardian.presentation.camera.CameraViewModel
import com.lmartin.qrguardian.presentation.result.ResultScreen
import com.lmartin.qrguardian.presentation.result.ResultUiState
import com.lmartin.qrguardian.presentation.result.ResultViewModel
import com.lmartin.qrguardian.presentation.theme.QrGuardianTheme
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    App(
        analyzeQr = { demoResult() },
    )
}

@Composable
fun App(
    cameraPermissionState: CameraPermissionState = CameraPermissionState.granted(),
    onRequestCameraPermission: ((Boolean) -> Unit) -> Unit = {},
    onOpenCameraSettings: () -> Unit = {},
    analyzeQr: suspend (String) -> QrAnalysisResult,
) {
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Intro) }
    var hasHandledScan by remember { mutableStateOf(false) }
    var showPermissionMessage by remember { mutableStateOf(false) }
    var waitingForSettingsReturn by remember { mutableStateOf(false) }
    val cameraViewModel = remember { CameraViewModel() }
    val resultViewModel = remember { ResultViewModel(ResultUiState.idle()) }

    LaunchedEffect(currentScreen) {
        if (currentScreen == AppScreen.Camera) {
            cameraViewModel.setScanning(true)
            cameraViewModel.setError(null)
            hasHandledScan = false
        }
    }

    LaunchedEffect(cameraPermissionState.isGranted, waitingForSettingsReturn) {
        if (cameraPermissionState.isGranted) {
            showPermissionMessage = false
            if (waitingForSettingsReturn) {
                waitingForSettingsReturn = false
                currentScreen = AppScreen.Camera
            }
        } else if (!cameraPermissionState.canRequestAgain) {
            showPermissionMessage = false
            waitingForSettingsReturn = false
        }
    }

    LaunchedEffect(currentScreen, cameraPermissionState.isGranted) {
        if (currentScreen == AppScreen.Camera && !cameraPermissionState.isGranted) {
            currentScreen = AppScreen.Intro
        }
    }

    QrGuardianTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
        ) {
            when (currentScreen) {
                AppScreen.Intro -> IntroScreen(
                    onStartScanningClick = {
                        when {
                            cameraPermissionState.isGranted -> {
                                showPermissionMessage = false
                                currentScreen = AppScreen.Camera
                            }

                            cameraPermissionState.canRequestAgain -> {
                                onRequestCameraPermission { granted ->
                                    if (granted) {
                                        showPermissionMessage = false
                                        currentScreen = AppScreen.Camera
                                    } else {
                                        showPermissionMessage = true
                                    }
                                }
                            }

                            else -> {
                                showPermissionMessage = false
                            }
                        }
                    },
                    showPermissionMessage = showPermissionMessage,
                    showPermissionSettingsCard = !cameraPermissionState.isGranted && !cameraPermissionState.canRequestAgain,
                    onOpenSettingsClick = {
                        showPermissionMessage = false
                        waitingForSettingsReturn = true
                        onOpenCameraSettings()
                    },
                )

                AppScreen.Camera -> CameraScreen(
                    viewModel = cameraViewModel,
                    onCloseClick = {
                        currentScreen = AppScreen.Intro
                        resultViewModel.reset()
                        cameraViewModel.setScanning(false)
                        cameraViewModel.setError(null)
                    },
                    onScanResult = { rawText ->
                        if (hasHandledScan) return@CameraScreen
                        hasHandledScan = true
                        resultViewModel.showLoading()
                        currentScreen = AppScreen.Result
                        scope.launch {
                            runCatching { analyzeQr(rawText) }
                                .onSuccess { resultViewModel.showResult(it) }
                                .onFailure { throwable ->
                                    resultViewModel.showError(
                                        throwable.message ?: "Unable to analyze the scanned content."
                                    )
                                }
                        }
                    },
                )

                AppScreen.Result -> ResultScreen(
                    viewModel = resultViewModel,
                    onOpenLinkClick = { uriHandler.openUri(it) },
                    onRescanClick = {
                        resultViewModel.reset()
                        currentScreen = if (cameraPermissionState.isGranted) {
                            AppScreen.Camera
                        } else {
                            AppScreen.Intro
                        }
                    },
                )
            }
        }
    }
}

private enum class AppScreen {
    Intro,
    Camera,
    Result,
}

private fun demoResult(): QrAnalysisResult {
    return QrAnalysisResult(
        originalText = "https://secure-login.example.com/account",
        normalizedText = "https://secure-login.example.com/account",
        contentType = com.lmartin.qrguardian.domain.model.QrContentType.Url,
        overallLevel = com.lmartin.qrguardian.domain.model.SecurityLevel.Suspicious,
        canOpen = true,
        localScan = com.lmartin.qrguardian.domain.model.ScanSectionResult(
            name = "Local scan",
            level = com.lmartin.qrguardian.domain.model.SecurityLevel.Suspicious,
            status = com.lmartin.qrguardian.domain.model.ScanStatus.Completed,
            title = "Phishing-like patterns detected",
            description = "The destination looks like a login or credential collection flow.",
            reasons = listOf(
                "The domain mirrors a login pattern.",
                "The path suggests a sign-in flow.",
                "The destination asks for user action.",
            ),
            metadata = listOf(
                com.lmartin.qrguardian.domain.model.ScanMetadataItem(label = "Content", value = "URL"),
            ),
        ),
        remoteReputation = com.lmartin.qrguardian.domain.model.ScanSectionResult(
            name = "Remote reputation",
            level = com.lmartin.qrguardian.domain.model.SecurityLevel.Unknown,
            status = com.lmartin.qrguardian.domain.model.ScanStatus.Unavailable,
            title = "Reputation not available",
            description = "Remote checks are not available in this demo state.",
            reasons = listOf(
                "No backend reputation provider is configured.",
                "The app keeps the result visible before opening the link.",
            ),
            metadata = listOf(
                com.lmartin.qrguardian.domain.model.ScanMetadataItem(label = "Provider", value = "Not configured"),
                com.lmartin.qrguardian.domain.model.ScanMetadataItem(label = "Last check", value = "Unavailable"),
            ),
        ),
    )
}

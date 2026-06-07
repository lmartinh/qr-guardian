package com.lmartin.qrguardian.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
class QrGuardianAppState internal constructor(
    private val screenState: () -> AppScreen,
    private val setScreenState: (AppScreen) -> Unit,
    private val showPermissionMessageState: () -> Boolean,
    private val setShowPermissionMessageState: (Boolean) -> Unit,
    private val waitingForSettingsReturnState: () -> Boolean,
    private val setWaitingForSettingsReturnState: (Boolean) -> Unit,
    private val isProcessingScanState: () -> Boolean,
    private val setIsProcessingScanState: (Boolean) -> Unit,
    private val hasAcceptedScanState: () -> Boolean,
    private val setHasAcceptedScanState: (Boolean) -> Unit,
    private val pendingAnalysisRawTextState: () -> String?,
    private val setPendingAnalysisRawTextState: (String?) -> Unit,
    private val isCameraPermissionGrantedState: () -> Boolean,
    private val setCameraPermissionGrantedState: (Boolean) -> Unit,
) {
    val screen: AppScreen
        get() = screenState()

    val showPermissionMessage: Boolean
        get() = showPermissionMessageState()

    val waitingForSettingsReturn: Boolean
        get() = waitingForSettingsReturnState()

    val isProcessingScan: Boolean
        get() = isProcessingScanState()

    val hasAcceptedScan: Boolean
        get() = hasAcceptedScanState()

    val pendingAnalysisRawText: String?
        get() = pendingAnalysisRawTextState()

    val isCameraPermissionGranted: Boolean
        get() = isCameraPermissionGrantedState()

    fun onStartScan() {
        setShowPermissionMessageState(false)
    }

    fun onCameraPermissionGranted() {
        setCameraPermissionGrantedState(true)
        setShowPermissionMessageState(false)
        setWaitingForSettingsReturnState(false)
        setScreenState(AppScreen.Camera)
    }

    fun onCameraPermissionDenied() {
        setCameraPermissionGrantedState(false)
        setShowPermissionMessageState(true)
        setWaitingForSettingsReturnState(false)
    }

    fun onOpenCameraSettings() {
        setShowPermissionMessageState(false)
        setWaitingForSettingsReturnState(true)
    }

    fun syncCameraPermissionState(isGranted: Boolean) {
        setCameraPermissionGrantedState(isGranted)
    }

    fun onQrDetected(rawText: String): Boolean {
        if (rawText.isBlank() || screenState() != AppScreen.Camera || isProcessingScanState() || hasAcceptedScanState()) {
            return false
        }

        setIsProcessingScanState(true)
        setHasAcceptedScanState(true)
        setPendingAnalysisRawTextState(rawText)
        setScreenState(AppScreen.Result)
        return true
    }

    fun onAnalysisResult() {
        setIsProcessingScanState(false)
    }

    fun onAnalysisError() {
        setIsProcessingScanState(false)
    }

    fun onRescan() {
        setIsProcessingScanState(false)
        setHasAcceptedScanState(false)
        setPendingAnalysisRawTextState(null)
        setWaitingForSettingsReturnState(false)
        setScreenState(if (isCameraPermissionGrantedState()) AppScreen.Camera else AppScreen.Intro)
    }

    fun onCloseCamera() {
        setIsProcessingScanState(false)
        setHasAcceptedScanState(false)
        setPendingAnalysisRawTextState(null)
        setWaitingForSettingsReturnState(false)
        setScreenState(AppScreen.Intro)
    }

    fun onBackToIntro() {
        setWaitingForSettingsReturnState(false)
        setScreenState(AppScreen.Intro)
    }
}

@Composable
fun rememberQrGuardianAppState(
    initialCameraPermissionGranted: Boolean,
): QrGuardianAppState {
    var screen by rememberSaveable { mutableStateOf(AppScreen.Intro) }
    var showPermissionMessage by rememberSaveable { mutableStateOf(false) }
    var waitingForSettingsReturn by rememberSaveable { mutableStateOf(false) }
    var isProcessingScan by rememberSaveable { mutableStateOf(false) }
    var hasAcceptedScan by rememberSaveable { mutableStateOf(false) }
    var pendingAnalysisRawText by rememberSaveable { mutableStateOf<String?>(null) }
    var isCameraPermissionGranted by rememberSaveable { mutableStateOf(initialCameraPermissionGranted) }

    return remember {
        QrGuardianAppState(
            screenState = { screen },
            setScreenState = { screen = it },
            showPermissionMessageState = { showPermissionMessage },
            setShowPermissionMessageState = { showPermissionMessage = it },
            waitingForSettingsReturnState = { waitingForSettingsReturn },
            setWaitingForSettingsReturnState = { waitingForSettingsReturn = it },
            isProcessingScanState = { isProcessingScan },
            setIsProcessingScanState = { isProcessingScan = it },
            hasAcceptedScanState = { hasAcceptedScan },
            setHasAcceptedScanState = { hasAcceptedScan = it },
            pendingAnalysisRawTextState = { pendingAnalysisRawText },
            setPendingAnalysisRawTextState = { pendingAnalysisRawText = it },
            isCameraPermissionGrantedState = { isCameraPermissionGranted },
            setCameraPermissionGrantedState = { isCameraPermissionGranted = it },
        )
    }
}

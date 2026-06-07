package com.lmartin.qrguardian.presentation.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QrGuardianAppStateTest {
    @Test
    fun `initial state starts on intro`() {
        val harness = createHarness()

        assertEquals(AppScreen.Intro, harness.state.screen)
        assertFalse(harness.state.isProcessingScan)
        assertFalse(harness.state.hasAcceptedScan)
        assertEquals(null, harness.state.pendingAnalysisRawText)
    }

    @Test
    fun `permission granted start scan moves to camera`() {
        val harness = createHarness()

        harness.state.onStartScan()
        harness.state.onCameraPermissionGranted()

        assertEquals(AppScreen.Camera, harness.state.screen)
        assertTrue(harness.state.isCameraPermissionGranted)
    }

    @Test
    fun `blank qr is ignored`() {
        val harness = createHarness(initialPermissionGranted = true)
        harness.state.onCameraPermissionGranted()

        val accepted = harness.state.onQrDetected("   ")

        assertFalse(accepted)
        assertEquals(AppScreen.Camera, harness.state.screen)
        assertFalse(harness.state.isProcessingScan)
        assertFalse(harness.state.hasAcceptedScan)
        assertEquals(null, harness.state.pendingAnalysisRawText)
    }

    @Test
    fun `valid qr is accepted once`() {
        val harness = createHarness(initialPermissionGranted = true)
        harness.state.onCameraPermissionGranted()

        val accepted = harness.state.onQrDetected("https://example.com")
        val duplicate = harness.state.onQrDetected("https://example.com")

        assertTrue(accepted)
        assertFalse(duplicate)
        assertEquals(AppScreen.Result, harness.state.screen)
        assertTrue(harness.state.isProcessingScan)
        assertTrue(harness.state.hasAcceptedScan)
        assertEquals("https://example.com", harness.state.pendingAnalysisRawText)
    }

    @Test
    fun `analysis result clears processing`() {
        val harness = createHarness(initialPermissionGranted = true)
        harness.state.onCameraPermissionGranted()
        harness.state.onQrDetected("https://example.com")

        harness.state.onAnalysisResult()

        assertFalse(harness.state.isProcessingScan)
        assertEquals(AppScreen.Result, harness.state.screen)
    }

    @Test
    fun `analysis error clears processing`() {
        val harness = createHarness(initialPermissionGranted = true)
        harness.state.onCameraPermissionGranted()
        harness.state.onQrDetected("https://example.com")

        harness.state.onAnalysisError()

        assertFalse(harness.state.isProcessingScan)
        assertEquals(AppScreen.Result, harness.state.screen)
    }

    @Test
    fun `rescan clears gating and returns to camera when permission is granted`() {
        val harness = createHarness(initialPermissionGranted = true)
        harness.state.onCameraPermissionGranted()
        harness.state.onQrDetected("https://example.com")
        harness.state.onAnalysisResult()

        harness.state.onRescan()

        assertEquals(AppScreen.Camera, harness.state.screen)
        assertFalse(harness.state.isProcessingScan)
        assertFalse(harness.state.hasAcceptedScan)
        assertEquals(null, harness.state.pendingAnalysisRawText)
    }

    @Test
    fun `close camera returns to intro and resets gating`() {
        val harness = createHarness(initialPermissionGranted = true)
        harness.state.onCameraPermissionGranted()
        harness.state.onQrDetected("https://example.com")

        harness.state.onCloseCamera()

        assertEquals(AppScreen.Intro, harness.state.screen)
        assertFalse(harness.state.isProcessingScan)
        assertFalse(harness.state.hasAcceptedScan)
        assertEquals(null, harness.state.pendingAnalysisRawText)
    }

    @Test
    fun `new qr can be processed after rescan`() {
        val harness = createHarness(initialPermissionGranted = true)
        harness.state.onCameraPermissionGranted()
        harness.state.onQrDetected("https://example.com")
        harness.state.onAnalysisResult()
        harness.state.onRescan()

        val acceptedAgain = harness.state.onQrDetected("https://example.org")

        assertTrue(acceptedAgain)
        assertEquals(AppScreen.Result, harness.state.screen)
        assertEquals("https://example.org", harness.state.pendingAnalysisRawText)
    }

    private fun createHarness(initialPermissionGranted: Boolean = false): Harness {
        var screen = AppScreen.Intro
        var showPermissionMessage = false
        var waitingForSettingsReturn = false
        var isProcessingScan = false
        var hasAcceptedScan = false
        var pendingAnalysisRawText: String? = null
        var isCameraPermissionGranted = initialPermissionGranted

        val state = QrGuardianAppState(
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

        return Harness(
            state = state,
            getShowPermissionMessage = { showPermissionMessage },
            getWaitingForSettingsReturn = { waitingForSettingsReturn },
        )
    }

    private data class Harness(
        val state: QrGuardianAppState,
        val getShowPermissionMessage: () -> Boolean,
        val getWaitingForSettingsReturn: () -> Boolean,
    )
}

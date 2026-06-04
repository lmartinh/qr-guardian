@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.lmartin.qrguardian.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
import platform.AVFoundation.AVMetadataObjectTypePDF417Code
import platform.AVFoundation.AVMetadataObjectTypeAztecCode
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeCode93Code
import platform.AVFoundation.AVMetadataObjectTypeDataMatrixCode
import platform.AVFoundation.AVMetadataObjectTypeInterleaved2of5Code
import platform.AVFoundation.AVMetadataObjectTypeITF14Code
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectZero
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import platform.UIKit.UIView
import platform.UIKit.UIColor
import kotlinx.cinterop.readValue

@Composable
actual fun PlatformQrScanner(
    isActive: Boolean,
    isTorchEnabled: Boolean,
    onScanResult: (String) -> Unit,
    onScannerError: (String) -> Unit,
    onTorchAvailabilityChanged: (Boolean) -> Unit,
    modifier: Modifier,
) {
    val onScanResultState = rememberUpdatedState(onScanResult)
    val onScannerErrorState = rememberUpdatedState(onScannerError)
    val onTorchAvailabilityChangedState = rememberUpdatedState(onTorchAvailabilityChanged)
    val scannerView = remember { QrScannerView() }

    UIKitView(
        factory = { scannerView },
        modifier = modifier,
        update = { view ->
            view.onScanResult = { value -> onScanResultState.value(value) }
            view.onScannerError = { message -> onScannerErrorState.value(message) }
            view.onTorchAvailabilityChanged = { available -> onTorchAvailabilityChangedState.value(available) }
            view.setActive(isActive)
            view.setTorchEnabled(isTorchEnabled)
        },
    )

    DisposableEffect(isActive) {
        if (!isActive) {
            scannerView.stop()
        } else {
            scannerView.start()
        }
        onDispose {
            scannerView.stop()
        }
    }
}

private class QrScannerView : UIView(frame = CGRectZero.readValue()) {
    private val captureSession = AVCaptureSession()
    private val metadataOutput = AVCaptureMetadataOutput()
    private var previewLayer: AVCaptureVideoPreviewLayer? = null
    private var isConfigured = false
    private var isActive = false

    var onScanResult: ((String) -> Unit)? = null
    var onScannerError: ((String) -> Unit)? = null
    var onTorchAvailabilityChanged: ((Boolean) -> Unit)? = null

    init {
        backgroundColor = UIColor.clearColor
    }

    fun setActive(active: Boolean) {
        isActive = active
        if (active) {
            start()
        } else {
            stop()
        }
    }

    fun setTorchEnabled(enabled: Boolean) {
        // Torch control can be wired later once the platform-specific contract is stabilized.
    }

    fun start() {
        if (!isActive) return
        configureIfNeeded()
        if (!captureSession.running) {
            captureSession.startRunning()
        }
    }

    fun stop() {
        if (captureSession.running) {
            captureSession.stopRunning()
        }
    }

    private fun configureIfNeeded() {
        if (isConfigured) return

        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> Unit
            AVAuthorizationStatusDenied,
            AVAuthorizationStatusRestricted,
            AVAuthorizationStatusNotDetermined -> {
                onScannerError?.invoke("Camera permission is required.")
                return
            }
        }

        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            ?: run {
                onScannerError?.invoke("No camera device is available.")
                return
            }

        val input = try {
            AVCaptureDeviceInput(device = device, error = null)
        } catch (exception: Throwable) {
            onScannerError?.invoke(exception.message ?: "Unable to create camera input.")
            return
        }

        if (captureSession.canAddInput(input)) {
            captureSession.addInput(input)
        } else {
            onScannerError?.invoke("Unable to configure the camera input.")
            return
        }

        if (captureSession.canAddOutput(metadataOutput)) {
            captureSession.addOutput(metadataOutput)
        } else {
            onScannerError?.invoke("Unable to configure QR scanning.")
            return
        }

        metadataOutput.setMetadataObjectsDelegate(
            objectsDelegate = QrMetadataDelegate(
                onScanResult = { rawValue ->
                    onScanResult?.invoke(rawValue)
                },
                onError = { message ->
                    onScannerError?.invoke(message)
                },
            ),
            queue = dispatch_get_main_queue(),
        )
        metadataOutput.metadataObjectTypes = listOf(
            AVMetadataObjectTypeQRCode,
            AVMetadataObjectTypeEAN13Code,
            AVMetadataObjectTypeEAN8Code,
            AVMetadataObjectTypeUPCECode,
            AVMetadataObjectTypePDF417Code,
            AVMetadataObjectTypeAztecCode,
            AVMetadataObjectTypeCode128Code,
            AVMetadataObjectTypeCode39Code,
            AVMetadataObjectTypeCode93Code,
            AVMetadataObjectTypeDataMatrixCode,
            AVMetadataObjectTypeInterleaved2of5Code,
            AVMetadataObjectTypeITF14Code,
        )

        previewLayer = AVCaptureVideoPreviewLayer(session = captureSession).apply {
            videoGravity = AVLayerVideoGravityResizeAspectFill
            frame = bounds
        }
        previewLayer?.let { layer.addSublayer(it) }

        onTorchAvailabilityChanged?.invoke(false)
        isConfigured = true
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        previewLayer?.frame = bounds
    }
}

private class QrMetadataDelegate(
    private val onScanResult: (String) -> Unit,
    private val onError: (String) -> Unit,
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
    override fun captureOutput(
        output: platform.AVFoundation.AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: platform.AVFoundation.AVCaptureConnection,
    ) {
        val rawValue = didOutputMetadataObjects
            .asSequence()
            .mapNotNull { it as? AVMetadataMachineReadableCodeObject }
            .mapNotNull { it.stringValue }
            .firstOrNull()

        if (rawValue.isNullOrBlank()) return
        onScanResult(rawValue)
    }
}

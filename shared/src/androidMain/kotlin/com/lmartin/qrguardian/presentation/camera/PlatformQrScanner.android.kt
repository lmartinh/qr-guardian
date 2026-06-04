package com.lmartin.qrguardian.presentation.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

@Composable
actual fun PlatformQrScanner(
    isActive: Boolean,
    isTorchEnabled: Boolean,
    onScanResult: (String) -> Unit,
    onScannerError: (String) -> Unit,
    onTorchAvailabilityChanged: (Boolean) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val onScanResultState = rememberUpdatedState(onScanResult)
    val onScannerErrorState = rememberUpdatedState(onScannerError)
    val onTorchAvailabilityChangedState = rememberUpdatedState(onTorchAvailabilityChanged)
    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS)
                .build(),
        )
    }
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        }
    }
    val scanConsumed = remember { AtomicBoolean(false) }

    AndroidView(
        factory = { previewContext ->
            PreviewView(previewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                controller = cameraController
            }
        },
        modifier = modifier,
        update = { previewView ->
            previewView.controller = cameraController
        },
    )

    DisposableEffect(isActive) {
        if (isActive) {
            try {
                cameraController.bindToLifecycle(lifecycleOwner)
                onTorchAvailabilityChangedState.value.invoke(
                    cameraController.cameraInfo?.hasFlashUnit() == true,
                )
                cameraController.setImageAnalysisAnalyzer(
                    ContextCompat.getMainExecutor(context),
                ) { imageProxy ->
                    analyzeFrame(
                        imageProxy = imageProxy,
                        barcodeScanner = barcodeScanner,
                        onScanResult = onScanResultState.value,
                        onScannerError = onScannerErrorState.value,
                        scanConsumed = scanConsumed,
                    )
                }
                if (cameraController.cameraInfo?.hasFlashUnit() == true) {
                    cameraController.enableTorch(isTorchEnabled)
                }
            } catch (throwable: Throwable) {
                onScannerErrorState.value(
                    throwable.message ?: "Unable to start the camera scanner.",
                )
            }
        } else {
            cameraController.clearImageAnalysisAnalyzer()
            cameraController.enableTorch(false)
            onTorchAvailabilityChangedState.value(false)
        }

        onDispose {
            cameraController.clearImageAnalysisAnalyzer()
            cameraController.enableTorch(false)
            onTorchAvailabilityChangedState.value(false)
            barcodeScanner.close()
            runCatching { cameraController.unbind() }
        }
    }

    LaunchedEffect(isTorchEnabled, isActive) {
        if (isActive && cameraController.cameraInfo?.hasFlashUnit() == true) {
            runCatching { cameraController.enableTorch(isTorchEnabled) }
        }
    }
}

private fun analyzeFrame(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onScanResult: (String) -> Unit,
    onScannerError: (String) -> Unit,
    scanConsumed: AtomicBoolean,
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    barcodeScanner.process(image)
        .addOnSuccessListener { barcodes ->
            val rawValue = barcodes.firstNotNullOfOrNull { it.rawValue }?.trim()
            if (!rawValue.isNullOrBlank() && scanConsumed.compareAndSet(false, true)) {
                onScanResult(rawValue)
            }
        }
        .addOnFailureListener { throwable ->
            onScannerError(throwable.message ?: "Unable to scan the code.")
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

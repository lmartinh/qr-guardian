package com.lmartin.qrguardian

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.lmartin.qrguardian.core.network.QrGuardianHttpClientFactory
import com.lmartin.qrguardian.core.security.QrGuardianSecurityPipelineFactory
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import io.ktor.client.engine.okhttp.OkHttp

@Composable
fun AndroidAppRoot() {
    val context = LocalContext.current
    var cameraPermissionGranted by remember {
        mutableStateOf(hasCameraPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        cameraPermissionGranted = granted
    }

    val httpClient = remember {
        QrGuardianHttpClientFactory.create(OkHttp)
    }

    val analyzeQr: suspend (String) -> QrAnalysisResult = remember(httpClient) {
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
            permissionLauncher.launch(Manifest.permission.CAMERA)
        },
        analyzeQr = analyzeQr,
    )
}

private fun hasCameraPermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED
}

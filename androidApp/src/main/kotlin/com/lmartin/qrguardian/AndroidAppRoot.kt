package com.lmartin.qrguardian

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lmartin.qrguardian.core.network.QrGuardianHttpClientFactory
import com.lmartin.qrguardian.core.security.QrGuardianSecurityPipelineFactory
import com.lmartin.qrguardian.data.reputation.createRemoteReputationConfig
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.presentation.permissions.CameraPermissionState
import io.ktor.client.engine.okhttp.OkHttp

@Composable
fun AndroidAppRoot() {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val preferences = remember {
        context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
    }
    var hasRequestedPermission by remember {
        mutableStateOf(preferences.getBoolean(KEY_CAMERA_PERMISSION_REQUESTED, false))
    }
    var cameraPermissionState by remember {
        mutableStateOf(
            resolveCameraPermissionState(
                context = context,
                activity = activity,
                hasRequestedPermission = hasRequestedPermission,
            ),
        )
    }
    var permissionResultCallback by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasRequestedPermission = true
        preferences.edit()
            .putBoolean(KEY_CAMERA_PERMISSION_REQUESTED, true)
            .apply()
        cameraPermissionState = resolveCameraPermissionState(
            context = context,
            activity = activity,
            hasRequestedPermission = hasRequestedPermission,
        )
        permissionResultCallback?.invoke(granted)
        permissionResultCallback = null
    }

    val httpClient = remember {
        QrGuardianHttpClientFactory.create(OkHttp)
    }

    val remoteReputationConfig = remember {
        createRemoteReputationConfig(
            googleSafeBrowsingApiKey = BuildConfig.GOOGLE_SAFE_BROWSING_API_KEY,
            urlHausApiKey = BuildConfig.URLHAUS_API_KEY
        )
    }
    val analyzeQr: suspend (String) -> QrAnalysisResult = remember(httpClient) {
        QrGuardianSecurityPipelineFactory.createAnalyzeQrSafetyUseCase(
            httpClient = httpClient,
            remoteReputationConfig = remoteReputationConfig
        )::invoke
    }

    DisposableEffect(Unit) {
        onDispose {
            httpClient.close()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                cameraPermissionState = resolveCameraPermissionState(
                    context = context,
                    activity = activity,
                    hasRequestedPermission = hasRequestedPermission,
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    App(
        cameraPermissionState = cameraPermissionState,
        onRequestCameraPermission = { onResult ->
            hasRequestedPermission = true
            preferences.edit()
                .putBoolean(KEY_CAMERA_PERMISSION_REQUESTED, true)
                .apply()
            permissionResultCallback = onResult
            permissionLauncher.launch(Manifest.permission.CAMERA)
        },
        onOpenCameraSettings = {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            ).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        },
        analyzeQr = analyzeQr,
    )
}

private const val PREFERENCES_NAME = "qr_guardian_permissions"
private const val KEY_CAMERA_PERMISSION_REQUESTED = "camera_permission_requested"

private fun resolveCameraPermissionState(
    context: android.content.Context,
    activity: Activity?,
    hasRequestedPermission: Boolean,
): CameraPermissionState {
    val isGranted = hasCameraPermission(context)
    val canRequestAgain = isGranted || !hasRequestedPermission || (
        activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(
                it,
                Manifest.permission.CAMERA,
            )
        } == true
    )

    return CameraPermissionState(
        isGranted = isGranted,
        canRequestAgain = canRequestAgain,
    )
}

private fun hasCameraPermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED
}

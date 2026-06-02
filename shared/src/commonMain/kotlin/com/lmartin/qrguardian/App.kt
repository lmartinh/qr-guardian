package com.lmartin.qrguardian

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanMetadataItem
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.presentation.intro.IntroScreen
import com.lmartin.qrguardian.presentation.result.ResultScreen
import com.lmartin.qrguardian.presentation.result.ResultUiState
import com.lmartin.qrguardian.presentation.result.ResultViewModel
import com.lmartin.qrguardian.presentation.theme.QrGuardianTheme

@Composable
@Preview
fun App() {
    QrGuardianTheme {
        var launchRequested by remember { mutableStateOf(false) }
        val resultViewModel = remember { ResultViewModel(ResultUiState.success(demoResult())) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (launchRequested) {
                ResultScreen(
                    viewModel = resultViewModel,
                    onOpenLinkClick = { },
                    onRescanClick = { launchRequested = false },
                )
            } else {
                IntroScreen(
                    onStartScanningClick = { launchRequested = true },
                )
            }
        }
    }
}

private fun demoResult(): QrAnalysisResult {
    return QrAnalysisResult(
        originalText = "https://secure-login.example.com/account",
        normalizedText = "https://secure-login.example.com/account",
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

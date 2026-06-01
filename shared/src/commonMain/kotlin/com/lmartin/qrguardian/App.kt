package com.lmartin.qrguardian

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.lmartin.qrguardian.presentation.intro.IntroScreen
import com.lmartin.qrguardian.presentation.theme.QrGuardianTheme
import org.jetbrains.compose.resources.stringResource
import qrguardian.shared.generated.resources.Res
import qrguardian.shared.generated.resources.scanner_launching

@Composable
@Preview
fun App() {
    QrGuardianTheme {
        var launchRequested by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (launchRequested) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(Res.string.scanner_launching))
                }
            } else {
                IntroScreen(
                    onStartScanningClick = { launchRequested = true },
                )
            }
        }
    }
}

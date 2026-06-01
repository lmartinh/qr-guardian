package com.lmartin.qrguardian.presentation.intro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lmartin.qrguardian.presentation.components.QRGuardianPrimaryButton
import com.lmartin.qrguardian.presentation.theme.QrGuardianColors.PrimaryDark
import com.lmartin.qrguardian.presentation.theme.QrGuardianSpacing
import org.jetbrains.compose.resources.stringResource
import qrguardian.shared.generated.resources.Res
import qrguardian.shared.generated.resources.intro_badge
import qrguardian.shared.generated.resources.intro_description
import qrguardian.shared.generated.resources.intro_start_scanning
import qrguardian.shared.generated.resources.intro_title_highlight
import qrguardian.shared.generated.resources.intro_title_main

@Composable
fun IntroScreen(
    onStartScanningClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = QrGuardianSpacing.L, vertical = QrGuardianSpacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
        ) {
            Text(
                text = "✦ ${stringResource(Res.string.intro_badge)}",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(QrGuardianSpacing.L))
        Text(
            text = stringResource(Res.string.intro_title_main),
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(QrGuardianSpacing.S))
        Text(
            text = stringResource(Res.string.intro_title_highlight),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
            ),
            textAlign = TextAlign.Center,
            color = PrimaryDark,
        )
        Spacer(modifier = Modifier.height(QrGuardianSpacing.L))
        Text(
            text = stringResource(Res.string.intro_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(QrGuardianSpacing.Xxl))
        QRGuardianPrimaryButton(
            text = "${stringResource(Res.string.intro_start_scanning)}  →",
            onClick = onStartScanningClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
        )
    }
}

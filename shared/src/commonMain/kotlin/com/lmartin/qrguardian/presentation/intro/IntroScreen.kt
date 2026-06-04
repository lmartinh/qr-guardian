package com.lmartin.qrguardian.presentation.intro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.QrCodeScanner

@Composable
fun IntroScreen(
    onStartScanningClick: () -> Unit,
    showPermissionMessage: Boolean,
    showPermissionSettingsCard: Boolean,
    onOpenSettingsClick: () -> Unit,
) {
    val texts = rememberIntroTexts()

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
                text = "✦ ${texts.badge}",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(QrGuardianSpacing.L))
        Text(
            text = texts.titleMain,
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(QrGuardianSpacing.S))
        Text(
            text = texts.titleHighlight,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
            ),
            textAlign = TextAlign.Center,
            color = PrimaryDark,
        )
        Spacer(modifier = Modifier.height(QrGuardianSpacing.L))
        Text(
            text = texts.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(QrGuardianSpacing.Xxl))
        QRGuardianPrimaryButton(
            text = texts.startScanning,
            onClick = onStartScanningClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            },
        )

        if (showPermissionMessage) {
            Spacer(modifier = Modifier.height(QrGuardianSpacing.M))
            Text(
                text = texts.permissionMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (showPermissionSettingsCard) {
            Spacer(modifier = Modifier.height(QrGuardianSpacing.M))
            PermissionSettingsCard(
                texts = texts,
                onOpenSettingsClick = onOpenSettingsClick,
            )
        }
    }
}

@Composable
private fun PermissionSettingsCard(
    texts: IntroTexts,
    onOpenSettingsClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(QrGuardianSpacing.M),
            verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
        ) {
            Text(
                text = texts.permissionCardTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = texts.permissionCardMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            QRGuardianPrimaryButton(
                text = texts.permissionCardAction,
                onClick = onOpenSettingsClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

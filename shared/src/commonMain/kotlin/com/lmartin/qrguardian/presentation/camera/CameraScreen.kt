package com.lmartin.qrguardian.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lmartin.qrguardian.presentation.theme.QrGuardianColors
import com.lmartin.qrguardian.presentation.theme.QrGuardianSpacing

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onCloseClick: () -> Unit,
    onScanResult: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = viewModel.uiState
    val texts = rememberCameraTexts()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(horizontal = QrGuardianSpacing.M, vertical = QrGuardianSpacing.Xs),
            verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.M),
        ) {
            CameraTopBar(
                texts = texts,
                state = state,
                onCloseClick = onCloseClick,
                onTorchClick = viewModel::toggleTorch,
            )

            Text(
                text = texts.title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = texts.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            CameraScannerCard(
                texts = texts,
                state = state,
                onScanResult = onScanResult,
                onScannerError = viewModel::setError,
                onTorchAvailabilityChanged = viewModel::setTorchAvailable,
            )

            CameraInstructionCard(texts = texts)

            state.errorMessage?.let { errorMessage ->
                ErrorBanner(message = errorMessage)
            }
        }
    }
}

@Composable
private fun CameraTopBar(
    texts: CameraTexts,
    state: CameraUiState,
    onCloseClick: () -> Unit,
    onTorchClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = texts.close,
            )
        }

        if (state.isTorchAvailable) {
            OutlinedButton(
                onClick = onTorchClick,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(text = if (state.isTorchEnabled) texts.torchOn else texts.torchOff)
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun CameraScannerCard(
    texts: CameraTexts,
    state: CameraUiState,
    onScanResult: (String) -> Unit,
    onScannerError: (String) -> Unit,
    onTorchAvailabilityChanged: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(QrGuardianSpacing.M),
        ) {
            PlatformQrScanner(
                isActive = state.isScanning,
                isTorchEnabled = state.isTorchEnabled,
                onScanResult = onScanResult,
                onScannerError = onScannerError,
                onTorchAvailabilityChanged = onTorchAvailabilityChanged,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(250.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        width = 2.dp,
                        color = QrGuardianColors.PrimaryDark.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(28.dp),
                    )
                    .background(Color.Transparent),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = QrGuardianSpacing.S),
                shape = MaterialTheme.shapes.large,
                color = QrGuardianColors.PrimaryDark.copy(alpha = 0.72f),
            ) {
                Text(
                    text = if (state.isScanning) texts.scanning else texts.scannerPlaceholder,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun CameraInstructionCard(texts: CameraTexts) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(QrGuardianSpacing.M),
            verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.Xs),
        ) {
            Text(
                text = texts.hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

package com.lmartin.qrguardian.presentation.camera

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
                .padding(horizontal = QrGuardianSpacing.S)
                .padding(bottom = QrGuardianSpacing.S),
            verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.M),
        ) {
            CameraTopBar(
                texts = texts,
                state = state,
                onCloseClick = onCloseClick,
                onTorchClick = viewModel::toggleTorch,
            )

            CameraHeader(texts = texts)

            CameraScannerCard(
                texts = texts,
                state = state,
                onScanResult = onScanResult,
                onScannerError = viewModel::setError,
                onTorchAvailabilityChanged = viewModel::setTorchAvailable,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            state.errorMessage?.let { message ->
                CameraErrorBanner(message = message)
            } ?: CameraHintCard(texts = texts)
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
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledIconButton(
            onClick = onCloseClick,
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = texts.close,
            )
        }

        if (state.isTorchAvailable) {
            FilledIconButton(
                onClick = onTorchClick,
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (state.isTorchEnabled) {
                        QrGuardianColors.Primary.copy(alpha = 0.28f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                    },
                    contentColor = if (state.isTorchEnabled) {
                        QrGuardianColors.PrimaryDark
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ),
            ) {
                Icon(
                    imageVector = if (state.isTorchEnabled) {
                        Icons.Filled.FlashOn
                    } else {
                        Icons.Filled.FlashOff
                    },
                    contentDescription = if (state.isTorchEnabled) {
                        texts.torchOn
                    } else {
                        texts.torchOff
                    },
                )
            }
        } else {
            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
private fun CameraHeader(texts: CameraTexts) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = texts.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = texts.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CameraScannerCard(
    texts: CameraTexts,
    state: CameraUiState,
    onScanResult: (String) -> Unit,
    onScannerError: (String) -> Unit,
    onTorchAvailabilityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color.Black),
        ) {
            PlatformQrScanner(
                isActive = state.isScanning,
                isTorchEnabled = state.isTorchEnabled,
                onScanResult = onScanResult,
                onScannerError = onScannerError,
                onTorchAvailabilityChanged = onTorchAvailabilityChanged,
                modifier = Modifier.fillMaxSize(),
            )

            CameraScannerScrim()

            CameraFocusFrame(
                modifier = Modifier.align(Alignment.Center),
            )

            CameraStatusPill(
                text = if (state.isScanning) {
                    texts.scanning
                } else {
                    texts.scannerPlaceholder
                },
                isScanning = state.isScanning,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = QrGuardianSpacing.M),
            )
        }
    }
}

@Composable
private fun CameraScannerScrim() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.22f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.30f),
                    ),
                ),
            ),
    )
}

@Composable
private fun CameraFocusFrame(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(252.dp)
            .border(
                width = 1.dp,
                color = QrGuardianColors.Primary.copy(alpha = 0.28f),
                shape = RoundedCornerShape(30.dp),
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.72f)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            QrGuardianColors.Primary,
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

private enum class FocusCornerPosition {
    TopStart,
    TopEnd,
    BottomEnd,
    BottomStart,
}

@Composable
private fun CameraStatusPill(
    text: String,
    isScanning: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = if (isScanning) {
            QrGuardianColors.PrimaryDark.copy(alpha = 0.82f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        },
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isScanning) {
                            QrGuardianColors.Safe
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    ),
            )

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = if (isScanning) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun CameraHintCard(texts: CameraTexts) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
        ),
        tonalElevation = 0.dp,
    ) {
        Text(
            text = texts.hint,
            modifier = Modifier.padding(QrGuardianSpacing.M),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CameraErrorBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
        ),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

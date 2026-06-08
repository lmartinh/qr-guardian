package com.lmartin.qrguardian.presentation.result.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lmartin.qrguardian.presentation.result.ResultTexts
import com.lmartin.qrguardian.presentation.theme.QrGuardianSpacing

@Composable
internal fun ResultErrorContent(
    message: String,
    onRescanClick: () -> Unit,
    texts: ResultTexts,
) {
    ResultPlaceholder(
        icon = Icons.Filled.Error,
        iconColor = MaterialTheme.colorScheme.error,
        title = texts.errorTitle,
        message = message,
        actionLabel = texts.rescan,
        onActionClick = onRescanClick,
    )
}

@Composable
internal fun ResultEmptyContent(
    onRescanClick: () -> Unit,
    texts: ResultTexts,
) {
    ResultPlaceholder(
        icon = Icons.Filled.Warning,
        iconColor = MaterialTheme.colorScheme.primary,
        title = texts.idleTitle,
        message = texts.idleMessage,
        actionLabel = texts.rescan,
        onActionClick = onRescanClick,
    )
}

@Composable
internal fun ResultLoadingContent(texts: ResultTexts) {
    ResultPlaceholder(
        icon = Icons.Filled.Refresh,
        iconColor = MaterialTheme.colorScheme.primary,
        title = texts.loadingTitle,
        message = texts.loadingMessage,
        actionLabel = texts.loadingAction,
        onActionClick = {},
        actionEnabled = false,
    )
}

@Composable
private fun ResultPlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    actionEnabled: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = QrGuardianSpacing.M, vertical = QrGuardianSpacing.Xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = iconColor.copy(alpha = 0.14f),
        ) {
            Box(
                modifier = Modifier.size(88.dp),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(42.dp),
                )
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(QrGuardianSpacing.L))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(QrGuardianSpacing.S))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(QrGuardianSpacing.L))
        Button(
            onClick = onActionClick,
            enabled = actionEnabled,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(text = actionLabel)
        }
    }
}

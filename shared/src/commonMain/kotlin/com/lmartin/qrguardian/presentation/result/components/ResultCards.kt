package com.lmartin.qrguardian.presentation.result.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.presentation.result.ResultDetailItem
import com.lmartin.qrguardian.presentation.theme.QrGuardianSpacing
import io.ktor.sse.SPACE

@Composable
internal fun ResultSectionCard(
    title: String,
    summary: String,
    level: SecurityLevel,
    levelLabel: String,
    levelTint: Color,
    levelContentColor: Color,
    items: List<ResultDetailItem>,
    signalsTitle: String? = null,
    signals: List<String> = emptyList(),
    maxVisibleItems: Int? = 3,
    maxVisibleSignals: Int = 3,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = BorderStroke(1.dp, levelTint.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QrGuardianSpacing.M),
            verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.M),
        ) {
            ResultSectionHeader(
                title = title,
                summary = summary,
                levelLabel = levelLabel,
                levelTint = levelTint,
                levelContentColor = levelContentColor,
            )

            if (items.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S)) {
                    val visibleItems = maxVisibleItems?.let(items::take) ?: items
                    visibleItems.forEach { item ->
                        ResultDetailRow(item = item)
                    }
                }
            }

            if (signals.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S)) {
                    if (!signalsTitle.isNullOrBlank()) {
                        Text(
                            text = signalsTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    signals.take(maxVisibleSignals).forEach { signal ->
                        ResultReasonRow(
                            text = signal,
                            tintColor = levelContentColor,
                            level = level.toReasonIcon(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ResultContentCard(
    title: String,
    normalizedValue: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(QrGuardianSpacing.M),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = normalizedValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun ResultSectionHeader(
    title: String,
    summary: String,
    levelLabel: String,
    levelTint: Color,
    levelContentColor: Color,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            ResultStatusPill(
                label = levelLabel,
                backgroundColor = levelTint,
                contentColor = levelContentColor,
            )
        }
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun SecurityLevel.toReasonIcon(): ImageVector = when (this) {
    SecurityLevel.Safe -> Icons.Filled.CheckCircle
    SecurityLevel.Suspicious -> Icons.Filled.Warning
    SecurityLevel.Dangerous -> Icons.Filled.Error
    SecurityLevel.Unknown -> Icons.AutoMirrored.Filled.HelpOutline
}

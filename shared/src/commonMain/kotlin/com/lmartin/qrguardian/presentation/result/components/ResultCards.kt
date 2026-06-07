package com.lmartin.qrguardian.presentation.result.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lmartin.qrguardian.presentation.result.ResultDetailItem
import com.lmartin.qrguardian.presentation.theme.QrGuardianSpacing

@Composable
internal fun ResultSectionCard(
    title: String,
    summary: String,
    levelLabel: String,
    levelTint: Color,
    levelContentColor: Color,
    items: List<ResultDetailItem>,
    signalsTitle: String? = null,
    signals: List<String> = emptyList(),
    maxVisibleItems: Int? = 3,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
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
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ResultDetailRow(item = item)
                        }
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
                    signals.forEach { signal ->
                        ResultReasonRow(text = signal)
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
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QrGuardianSpacing.M),
            verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.M),
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
                maxLines = 2,
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.78f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ResultStatusPill(
            label = levelLabel,
            backgroundColor = levelTint,
            contentColor = levelContentColor,
        )
    }
}

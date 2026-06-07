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
                    items.take(3).forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ResultDetailRow(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ResultContentCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    items: List<ResultDetailItem>,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ResultCardIcon(
                    icon = icon,
                    accent = accent,
                )
                Column(modifier = Modifier.weight(1f)) {
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

            if (items.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S)) {
                    items.take(3).forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ResultDetailRow(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ResultTechnicalCard(
    title: String,
    items: List<ResultDetailItem>,
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

            if (items.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S)) {
                    items.take(3).forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ResultDetailRow(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ResultCardIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
) {
    androidx.compose.material3.Surface(
        shape = MaterialTheme.shapes.large,
        color = accent.copy(alpha = 0.12f),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
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
            )
        }
        ResultStatusPill(
            label = levelLabel,
            backgroundColor = levelTint,
            contentColor = levelContentColor,
        )
    }
}

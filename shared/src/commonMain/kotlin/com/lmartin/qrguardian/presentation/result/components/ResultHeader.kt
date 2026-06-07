package com.lmartin.qrguardian.presentation.result.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lmartin.qrguardian.presentation.result.ResultTone
import com.lmartin.qrguardian.presentation.theme.QrGuardianSpacing

@Composable
internal fun ResultHeader(
    tone: ResultTone,
    title: String,
    description: String,
    contentTypeLabel: String,
    statusLabel: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.M),
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            tone.accentColor.copy(alpha = 0.26f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.0f),
)
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(
                        color = tone.badgeContainerColor,
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tone.icon,
                    contentDescription = null,
                    tint = tone.badgeContentColor,
                    modifier = Modifier.size(46.dp),
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ResultStatusPill(
                label = contentTypeLabel,
                backgroundColor = tone.pillContainerColor,
                contentColor = tone.pillContentColor,
            )
            ResultStatusPill(
                label = statusLabel,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun ResultCallout(
    text: String,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

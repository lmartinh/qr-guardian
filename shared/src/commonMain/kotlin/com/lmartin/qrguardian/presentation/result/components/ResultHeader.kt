package com.lmartin.qrguardian.presentation.result.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val isDarkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val headerBackgroundColor = if (isDarkSurface) {
        tone.badgeContainerColor.copy(alpha = 0.22f)
    } else {
        tone.badgeContainerColor.copy(alpha = 0.52f)
    }
    val headerGlowColor = if (isDarkSurface) {
        tone.accentColor.copy(alpha = 0.14f)
    } else {
        tone.accentColor.copy(alpha = 0.24f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = headerBackgroundColor,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = tone.accentColor.copy(alpha = 0.18f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (isDarkSurface) headerBackgroundColor.copy(alpha = 0.92f) else tone.badgeContainerColor.copy(alpha = 0.72f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(horizontal = QrGuardianSpacing.M, vertical = QrGuardianSpacing.L),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.M),
        ) {
            Box(
                modifier = Modifier
                    .size(126.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                headerGlowColor,
                                Color.Transparent,
                            ),
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .border(
                            width = 1.dp,
                            color = tone.accentColor.copy(alpha = 0.16f),
                            shape = CircleShape,
                        )
                        .background(
                            color = tone.badgeContainerColor,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = tone.icon,
                        contentDescription = null,
                        tint = tone.badgeContentColor,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
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
}

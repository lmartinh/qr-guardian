package com.lmartin.qrguardian.presentation.result.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
internal fun ResultDetailRow(
    item: ResultDetailItem,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            shape = CircleShape,
            color = if (item.color == Color.Unspecified) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                item.color.copy(alpha = 0.12f)
            },
        ) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (item.color == Color.Unspecified) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        item.color
                    },
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth(0.82f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = item.value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun ResultReasonRow(
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun ResultStatusPill(
    label: String,
    backgroundColor: Color,
    contentColor: Color,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = backgroundColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
        )
    }
}

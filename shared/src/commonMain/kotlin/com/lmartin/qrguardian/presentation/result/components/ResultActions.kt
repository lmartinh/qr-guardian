package com.lmartin.qrguardian.presentation.result.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lmartin.qrguardian.presentation.result.ResultTone
import com.lmartin.qrguardian.presentation.theme.QrGuardianSpacing

@Composable
internal fun ResultOpenLinkButton(
    text: String,
    tone: ResultTone,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = tone.actionContainerColor,
            contentColor = tone.actionContentColor,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
            )
            Text(
                text = text,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
internal fun ResultRescanButton(
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
            )
            Text(
                text = text,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

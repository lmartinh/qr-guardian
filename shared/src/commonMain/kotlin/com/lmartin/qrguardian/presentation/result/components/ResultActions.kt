package com.lmartin.qrguardian.presentation.result.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.presentation.result.ResultTone
import com.lmartin.qrguardian.presentation.theme.QrGuardianSpacing

@Composable
internal fun ResultActionArea(
    showOpenButton: Boolean,
    overallLevel: SecurityLevel,
    openLinkText: String,
    rescanText: String,
    tone: ResultTone,
    onOpenClick: () -> Unit,
    onRescanClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
    ) {
        if (showOpenButton && overallLevel == SecurityLevel.Safe) {
            ResultOpenLinkButton(
                text = openLinkText,
                tone = tone,
                level = overallLevel,
                onClick = onOpenClick,
            )
            ResultRescanButton(
                text = rescanText,
                onClick = onRescanClick,
            )
        } else {
            ResultRescanButton(
                text = rescanText,
                onClick = onRescanClick,
            )
            if (showOpenButton) {
                ResultOpenLinkButton(
                    text = openLinkText,
                    tone = tone,
                    level = overallLevel,
                    onClick = onOpenClick,
                )
            }
        }
    }
}

@Composable
internal fun ResultOpenLinkButton(
    text: String,
    tone: ResultTone,
    level: SecurityLevel,
    onClick: () -> Unit,
) {
    if (level == SecurityLevel.Safe) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = MaterialTheme.shapes.large,
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
                    imageVector = Icons.Filled.OpenInBrowser,
                    contentDescription = null,
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenInBrowser,
                    contentDescription = null,
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
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
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = MaterialTheme.shapes.large,
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
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

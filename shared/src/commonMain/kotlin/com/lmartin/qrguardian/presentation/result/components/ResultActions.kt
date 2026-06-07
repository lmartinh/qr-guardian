package com.lmartin.qrguardian.presentation.result.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lmartin.qrguardian.presentation.result.ResultTone

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
        Text(
            text = text,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
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
        Text(
            text = text,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

package com.lmartin.qrguardian.presentation.result

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

internal data class ResultDetailItem(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val color: Color,
)

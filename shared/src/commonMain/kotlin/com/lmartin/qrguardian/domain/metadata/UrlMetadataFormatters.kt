package com.lmartin.qrguardian.domain.metadata

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.round

fun formatFileSize(bytes: Long): String {
    if (bytes < 0) {
        return "Unknown"
    }
    if (bytes < 1024) {
        return "$bytes B"
    }

    val units = arrayOf("KB", "MB", "GB", "TB")
    val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt()
    val unitIndex = (digitGroups - 1).coerceAtMost(units.lastIndex)
    val value = bytes / 1024.0.pow(unitIndex + 1)
    val rounded = round(value * 10) / 10
    val formattedValue =
        if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    return "$formattedValue ${units[unitIndex]}"
}

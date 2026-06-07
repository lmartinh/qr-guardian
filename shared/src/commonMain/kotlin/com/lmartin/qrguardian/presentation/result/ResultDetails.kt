package com.lmartin.qrguardian.presentation.result

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel

internal data class ResultDetailItem(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val color: Color,
)

internal fun buildContentDetails(
    analysis: QrAnalysisResult,
    texts: ResultTexts,
): List<ResultDetailItem> {
    val items = mutableListOf<ResultDetailItem>()

    items += detailItem(
        icon = Icons.Filled.Warning,
        label = texts.detailType,
        value = contentTypeLabel(analysis.contentType, texts),
        color = Color.Unspecified,
    )

    when (analysis.contentType) {
        QrContentType.Url -> {
            val downloadType = detectDownloadType(analysis.normalizedText, texts)
            val path = extractUrlPath(analysis.normalizedText)
            if (downloadType.isNotBlank()) {
                items += detailItem(Icons.Filled.Warning, texts.detailFile, downloadType)
            }
            if (path.isNotBlank()) {
                items += detailItem(Icons.Filled.Warning, texts.detailPath, path)
            }
        }
        QrContentType.Email -> {
            items += detailItem(Icons.Filled.Warning, texts.detailDestination, extractMailTarget(analysis.normalizedText, texts))
            items += detailItem(Icons.Filled.Warning, texts.detailExtraField, extractMailExtras(analysis.normalizedText, texts))
        }
        QrContentType.Phone -> {
            items += detailItem(Icons.Filled.Warning, texts.detailNumber, extractTelephoneTarget(analysis.normalizedText, texts))
        }
        QrContentType.Sms -> {
            items += detailItem(Icons.Filled.Warning, texts.detailDestination, extractSmsTarget(analysis.normalizedText, texts))
            items += detailItem(Icons.Filled.Warning, texts.detailMessage, extractSmsBody(analysis.normalizedText, texts))
        }
        QrContentType.Wifi -> {
            items += detailItem(Icons.Filled.Warning, texts.detailNetwork, extractWifiField(analysis.normalizedText, "S", texts))
            items += detailItem(Icons.Filled.Warning, texts.detailSecurity, extractWifiField(analysis.normalizedText, "T", texts))
        }
        QrContentType.VCard -> {
            items += detailItem(Icons.Filled.Warning, texts.detailContact, texts.importContactAction)
        }
        QrContentType.Geo -> {
            items += detailItem(Icons.Filled.Warning, texts.detailLocation, texts.openMapAction)
        }
        QrContentType.Crypto -> {
            items += detailItem(Icons.Filled.Warning, texts.detailPayment, texts.cryptoAction)
        }
        QrContentType.PlainText -> {
            items += detailItem(Icons.Filled.Warning, texts.detailUsage, texts.notALink)
        }
        QrContentType.Unknown -> {
            items += detailItem(Icons.Filled.Warning, texts.detailState, texts.notClassifiedPrecisely)
        }
    }

    return items.take(3)
}

internal fun buildLocalAnalysisDetails(
    analysis: QrAnalysisResult,
    texts: ResultTexts,
): List<ResultDetailItem> {
    val section = analysis.localScan
    val items = mutableListOf<ResultDetailItem>()

    items += detailItem(
        icon = when (section.level) {
            SecurityLevel.Safe -> Icons.Filled.CheckCircle
            SecurityLevel.Suspicious -> Icons.Filled.Warning
            SecurityLevel.Dangerous -> Icons.Filled.Warning
            SecurityLevel.Unknown -> Icons.Filled.Warning
        },
        label = texts.detailState,
        value = section.statusLabel(texts),
        color = section.level.toSignalColor(),
    )

    section.reasons.take(2).forEach { reason ->
        if (items.size < 3) {
            items += detailItem(Icons.Filled.Warning, texts.detailUsage, reason, section.level.toSignalColor())
        }
    }

    section.metadata.take(1).forEach { metadata ->
        if (items.size < 3) {
            items += detailItem(Icons.Filled.Warning, metadata.label, metadata.value)
        }
    }

    return items.take(3)
}

internal fun buildRemoteAnalysisDetails(
    section: ScanSectionResult,
    texts: ResultTexts,
): List<ResultDetailItem> {
    val items = mutableListOf<ResultDetailItem>()

    items += detailItem(
        icon = when (section.status) {
            ScanStatus.Completed -> Icons.Filled.CheckCircle
            ScanStatus.NotConfigured -> Icons.Filled.Warning
            ScanStatus.NotApplicable -> Icons.Filled.Warning
            ScanStatus.Unavailable -> Icons.Filled.Warning
        },
        label = texts.detailState,
        value = section.statusLabel(texts),
        color = section.level.toSignalColor(),
    )

    section.reasons.take(2).forEach { reason ->
        if (items.size < 3) {
            items += detailItem(Icons.Filled.Warning, texts.detailUsage, reason, section.level.toSignalColor())
        }
    }

    section.metadata.take(1).forEach { metadata ->
        if (items.size < 3) {
            items += detailItem(Icons.Filled.Warning, metadata.label, metadata.value)
        }
    }

    return items.take(3)
}

internal fun buildTechnicalDetails(
    analysis: QrAnalysisResult,
    texts: ResultTexts,
): List<ResultDetailItem> {
    val items = mutableListOf<ResultDetailItem>()

    items += detailItem(Icons.Filled.Warning, texts.detailType, analysis.contentType.name)
    items += detailItem(Icons.Filled.Warning, texts.detailDestination, analysis.openableUrl ?: texts.notALink)

    if (analysis.originalText != analysis.normalizedText) {
        items += detailItem(Icons.Filled.Warning, texts.detailUnknown, analysis.originalText)
    }

    return items.take(3)
}

internal fun SecurityLevel.toSignalColor(): Color {
    return when (this) {
        SecurityLevel.Safe -> Color(0xFF10B981)
        SecurityLevel.Suspicious -> Color(0xFF9A6B00)
        SecurityLevel.Dangerous -> Color(0xFFB42318)
        SecurityLevel.Unknown -> Color(0xFF6B7280)
    }
}

private fun detailItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color = Color.Unspecified,
): ResultDetailItem {
    return ResultDetailItem(
        icon = icon,
        label = label,
        value = value,
        color = color,
    )
}

private fun ScanSectionResult.statusLabel(texts: ResultTexts): String {
    return when (status) {
        ScanStatus.Completed -> texts.remoteCompleted
        ScanStatus.NotConfigured -> texts.remoteNotConfiguredStatus
        ScanStatus.NotApplicable -> texts.remoteNotApplicableStatus
        ScanStatus.Unavailable -> texts.remoteUnavailableStatus
    }
}

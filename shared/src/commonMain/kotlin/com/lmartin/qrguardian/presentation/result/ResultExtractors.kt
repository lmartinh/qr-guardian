package com.lmartin.qrguardian.presentation.result

import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.presentation.theme.QrGuardianColors

internal fun extractUrlPath(url: String): String {
    val withoutScheme = url.substringAfter("://", url)
    val pathStart = withoutScheme.indexOf('/')
    if (pathStart < 0) return ""
    val pathWithQuery = withoutScheme.substring(pathStart)
    return pathWithQuery.substringBefore('?')
}

internal fun detectDownloadType(url: String, texts: ResultTexts): String {
    val path = extractUrlPath(url).lowercase()
    return when {
        path.endsWith(".apk") -> texts.fileTypeApk
        path.endsWith(".pdf") -> texts.fileTypePdf
        path.endsWith(".zip") -> texts.fileTypeZip
        path.endsWith(".rar") -> texts.fileTypeRar
        path.endsWith(".7z") -> texts.fileTypeSevenZip
        path.endsWith(".doc") || path.endsWith(".docx") -> texts.fileTypeDocument
        path.endsWith(".xls") || path.endsWith(".xlsx") -> texts.fileTypeSpreadsheet
        path.endsWith(".ppt") || path.endsWith(".pptx") -> texts.fileTypePresentation
        path.endsWith(".mp3") || path.endsWith(".wav") -> texts.fileTypeAudio
        path.endsWith(".mp4") || path.endsWith(".mov") || path.endsWith(".mkv") -> texts.fileTypeVideo
        path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".webp") -> texts.fileTypeImage
        else -> ""
    }
}

internal fun extractMailTarget(text: String, texts: ResultTexts): String {
    return text.removePrefix("mailto:").substringBefore('?').ifBlank { texts.noValue }
}

internal fun extractMailExtras(text: String, texts: ResultTexts): String {
    val query = text.substringAfter('?', "")
    return when {
        query.isBlank() -> texts.noSubjectOrBody
        query.contains("subject=") || query.contains("body=") -> texts.predefinedFields
        else -> texts.parametersPresent
    }
}

internal fun extractTelephoneTarget(text: String, texts: ResultTexts): String {
    return text.removePrefix("tel:").ifBlank { texts.noValue }
}

internal fun extractSmsTarget(text: String, texts: ResultTexts): String {
    return text.removePrefix("sms:").substringBefore('?').ifBlank { texts.noValue }
}

internal fun extractSmsBody(text: String, texts: ResultTexts): String {
    val body = text.substringAfter("body=", "")
    return if (body.isBlank()) texts.noPredefinedMessage else body.substringBefore('&')
}

internal fun extractWifiField(text: String, key: String, texts: ResultTexts): String {
    val payload = text.removePrefix("WIFI:")
    return payload.split(';')
        .asSequence()
        .mapNotNull { entry ->
            val separatorIndex = entry.indexOf(':')
            if (separatorIndex <= 0) return@mapNotNull null
            val entryKey = entry.substring(0, separatorIndex).uppercase()
            val entryValue = entry.substring(separatorIndex + 1)
            if (entryKey == key) entryValue else null
        }
        .firstOrNull()
        .orEmpty()
        .ifBlank { texts.noValue }
}

internal fun contentTypeLabel(contentType: QrContentType, texts: ResultTexts): String {
    return when (contentType) {
        QrContentType.Url -> texts.urlLabel
        QrContentType.Email -> texts.detailContact
        QrContentType.Phone -> texts.phoneType
        QrContentType.Sms -> texts.smsType
        QrContentType.Wifi -> texts.wifiType
        QrContentType.VCard -> texts.vCardType
        QrContentType.Geo -> texts.locationType
        QrContentType.Crypto -> texts.cryptoType
        QrContentType.PlainText -> texts.plainTextType
        QrContentType.Unknown -> texts.unknownType
    }
}

internal fun contentActionLabel(
    contentType: QrContentType,
    openableUrl: String?,
    texts: ResultTexts,
): String {
    return when (contentType) {
        QrContentType.Url -> if (openableUrl.isNullOrBlank()) texts.notALink else texts.openLink
        QrContentType.Email -> texts.emailAction
        QrContentType.Phone -> texts.phoneAction
        QrContentType.Sms -> texts.smsAction
        QrContentType.Wifi -> texts.wifiAction
        QrContentType.VCard -> texts.importContactAction
        QrContentType.Geo -> texts.openMapAction
        QrContentType.Crypto -> texts.cryptoAction
        QrContentType.PlainText -> texts.notALink
        QrContentType.Unknown -> texts.notClassifiedPrecisely
    }
}

internal fun localScanSummary(
    section: ScanSectionResult,
    texts: ResultTexts,
): String {
    return section.title
}

internal fun localScanBadgeLabel(
    section: ScanSectionResult,
    texts: ResultTexts,
): String {
    return when (section.level) {
        SecurityLevel.Safe -> texts.localScanSafe
        SecurityLevel.Suspicious -> texts.localScanCareful
        SecurityLevel.Dangerous -> texts.statusBlocked
        SecurityLevel.Unknown -> texts.statusUncertain
    }
}

internal fun contentAccentColor(contentType: QrContentType): androidx.compose.ui.graphics.Color {
    return when (contentType) {
        QrContentType.Url -> QrGuardianColors.PrimaryDark
        QrContentType.Email -> QrGuardianColors.PrimaryDark
        QrContentType.Phone -> QrGuardianColors.PrimaryDark
        QrContentType.Sms -> QrGuardianColors.PrimaryDark
        QrContentType.Wifi -> QrGuardianColors.PrimaryDark
        QrContentType.VCard -> QrGuardianColors.PrimaryDark
        QrContentType.Geo -> QrGuardianColors.PrimaryDark
        QrContentType.Crypto -> QrGuardianColors.PrimaryDark
        QrContentType.PlainText -> QrGuardianColors.PrimaryDark
        QrContentType.Unknown -> QrGuardianColors.PrimaryDark
    }
}

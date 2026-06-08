package com.lmartin.qrguardian.domain.metadata

enum class UrlResourceKind {
    WebPage,
    Document,
    Image,
    Media,
    Archive,
    InstallerOrExecutable,
    UnknownBinary,
    OtherFile,
    Unknown,
}

internal fun isWebPageContentType(contentType: String?): Boolean {
    val normalized = contentType.orEmpty().lowercase()
    return normalized == "text/html" || normalized == "application/xhtml+xml"
}

internal fun isAttachmentDisposition(contentDisposition: String?): Boolean {
    val normalized = contentDisposition.orEmpty().lowercase()
    return normalized.contains("attachment")
}

internal fun extractFilenameFromContentDisposition(contentDisposition: String?): String? {
    val disposition = contentDisposition.orEmpty()
    val filenameStar =
        disposition
            .split(';')
            .firstOrNull { it.trim().startsWith("filename*=", ignoreCase = true) }
            ?.substringAfter('=')
            ?.trim()
            ?.trim('"')

    if (!filenameStar.isNullOrBlank()) {
        val value = filenameStar.substringAfter("''", filenameStar)
        return value.substringAfterLast('/')
    }

    val filename =
        disposition
            .split(';')
            .firstOrNull { it.trim().startsWith("filename=", ignoreCase = true) }
            ?.substringAfter('=')
            ?.trim()
            ?.trim('"')

    return filename?.takeIf { it.isNotBlank() }
}

internal fun inferFileNameFromPath(
    path: String,
    contentType: String?,
    contentDisposition: String?,
): String? {
    extractFilenameFromContentDisposition(contentDisposition)?.let { return it }

    if (isWebPageContentType(contentType) && !isAttachmentDisposition(contentDisposition)) {
        return null
    }

    val lastSegment = path.substringAfterLast('/')
    if (lastSegment.isBlank() || lastSegment == path) {
        return null
    }

    val extension = lastSegment.substringAfterLast('.', "")
    if (extension.isBlank() || extension == lastSegment) {
        return null
    }

    if (extension.lowercase() in WEB_PAGE_EXTENSIONS) {
        return null
    }

    return lastSegment
}

internal fun inferFileExtensionFromName(fileName: String?): String? {
    if (fileName.isNullOrBlank()) {
        return null
    }

    val extension = fileName.substringAfterLast('.', "")
    return extension.takeIf { it.isNotBlank() && it != fileName }
}

internal fun inferFileTypeFromExtension(
    fileName: String?,
    fileExtension: String?,
    contentType: String?,
): DownloadFileType {
    val normalizedExtension = (fileExtension ?: inferFileExtensionFromName(fileName)).orEmpty().lowercase()
    val normalizedContentType = contentType.orEmpty().lowercase()

    return when {
        normalizedContentType == "application/pdf" || normalizedExtension == "pdf" -> DownloadFileType.Pdf

        normalizedContentType.startsWith("image/") ||
            normalizedExtension in setOf("jpg", "jpeg", "png", "gif", "webp", "svg", "heic", "heif") -> DownloadFileType.Image

        normalizedContentType.startsWith("audio/") ||
            normalizedExtension in setOf("mp3", "wav", "aac", "ogg", "m4a", "flac") -> DownloadFileType.Audio

        normalizedContentType.startsWith("video/") ||
            normalizedExtension in setOf("mp4", "mov", "avi", "mkv", "webm", "m4v") -> DownloadFileType.Video

        normalizedContentType.startsWith("application/zip") ||
            normalizedContentType.contains("7z") ||
            normalizedContentType.contains("rar") ||
            normalizedContentType.contains("tar") ||
            normalizedContentType.contains("gzip") ||
            normalizedExtension in setOf("zip", "rar", "7z", "tar", "gz", "tgz", "bz2") -> DownloadFileType.Archive

        normalizedExtension in setOf("doc", "docx", "odt", "rtf", "txt", "md") -> DownloadFileType.Document

        normalizedExtension in setOf("xls", "xlsx", "csv", "ods") -> DownloadFileType.Spreadsheet

        normalizedExtension in setOf("ppt", "pptx", "odp") -> DownloadFileType.Presentation

        normalizedExtension in setOf("apk", "aab") ||
            normalizedContentType == "application/vnd.android.package-archive" -> DownloadFileType.AndroidApp

        normalizedExtension in setOf("dmg", "pkg") -> DownloadFileType.AppleDiskImage

        normalizedExtension in setOf("exe", "msi", "scr") ||
            normalizedContentType in setOf("application/x-msdownload", "application/x-msi", "application/x-msdos-program") -> DownloadFileType.WindowsExecutable

        normalizedExtension in setOf("js", "jar", "bat", "cmd", "sh", "ps1", "vbs") -> DownloadFileType.Script

        else -> DownloadFileType.Unknown
    }
}

internal fun inferUrlResourceKind(
    contentType: String?,
    contentDisposition: String?,
    fileName: String?,
    fileExtension: String?,
    fileType: DownloadFileType,
): UrlResourceKind {
    if (isWebPageContentType(contentType) && !isAttachmentDisposition(contentDisposition)) {
        return UrlResourceKind.WebPage
    }

    if (isAttachmentDisposition(contentDisposition)) {
        return resourceKindFromFileType(fileType, fileName, fileExtension, allowUnknownBinary = true)
    }

    return when {
        fileType != DownloadFileType.Unknown -> resourceKindFromFileType(fileType, fileName, fileExtension, allowUnknownBinary = true)
        contentType.equals("application/octet-stream", ignoreCase = true) -> UrlResourceKind.UnknownBinary
        fileName != null || !fileExtension.isNullOrBlank() -> UrlResourceKind.OtherFile
        else -> UrlResourceKind.WebPage
    }
}

internal fun shouldShowPath(
    path: String,
    fileName: String?,
    resourceKind: UrlResourceKind,
): Boolean {
    if (path.isBlank() || path == "/") {
        return false
    }

    if (resourceKind != UrlResourceKind.WebPage && fileName != null) {
        return false
    }

    return true
}

internal fun shouldShowDownload(
    contentDisposition: String?,
    resourceKind: UrlResourceKind,
): Boolean {
    if (isAttachmentDisposition(contentDisposition)) {
        return true
    }

    return resourceKind in setOf(
        UrlResourceKind.Archive,
        UrlResourceKind.InstallerOrExecutable,
        UrlResourceKind.UnknownBinary,
    )
}

private fun resourceKindFromFileType(
    fileType: DownloadFileType,
    fileName: String?,
    fileExtension: String?,
    allowUnknownBinary: Boolean,
): UrlResourceKind = when (fileType) {
    DownloadFileType.Pdf,
    DownloadFileType.Document,
    DownloadFileType.Spreadsheet,
    DownloadFileType.Presentation,
    -> UrlResourceKind.Document

    DownloadFileType.Image -> UrlResourceKind.Image

    DownloadFileType.Audio,
    DownloadFileType.Video,
    -> UrlResourceKind.Media

    DownloadFileType.Archive -> UrlResourceKind.Archive

    DownloadFileType.AndroidApp,
    DownloadFileType.AppleDiskImage,
    DownloadFileType.WindowsExecutable,
    DownloadFileType.Script,
    -> UrlResourceKind.InstallerOrExecutable

    DownloadFileType.Unknown -> when {
        allowUnknownBinary && (fileName == null && fileExtension.isNullOrBlank()) -> UrlResourceKind.UnknownBinary
        fileName != null || !fileExtension.isNullOrBlank() -> UrlResourceKind.OtherFile
        else -> UrlResourceKind.Unknown
    }
}

private val WEB_PAGE_EXTENSIONS =
    setOf(
        "html",
        "htm",
        "xhtml",
        "php",
        "asp",
        "aspx",
        "jsp",
        "shtml",
    )

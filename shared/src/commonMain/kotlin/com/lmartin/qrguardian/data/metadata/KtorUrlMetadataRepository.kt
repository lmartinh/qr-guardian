package com.lmartin.qrguardian.data.metadata

import com.lmartin.qrguardian.domain.metadata.DownloadFileType
import com.lmartin.qrguardian.domain.metadata.UrlMetadataRepository
import com.lmartin.qrguardian.domain.metadata.UrlMetadataResult
import com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus
import com.lmartin.qrguardian.domain.metadata.normalizeUrlForRequest
import com.lmartin.qrguardian.domain.rules.url.parseUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException

class KtorUrlMetadataRepository(
    private val httpClient: HttpClient,
) : UrlMetadataRepository {
    override suspend fun fetchMetadata(url: String): UrlMetadataResult {
        val normalizedRequestUrl = normalizeUrlForRequest(url)
        return runCatching {
            val response = httpClient.head(normalizedRequestUrl)
            if (response.status.value !in 200..299 || response.status == HttpStatusCode.MethodNotAllowed) {
                return unavailableResult()
            }

            mapResponse(response, normalizedRequestUrl)
        }.getOrElse { exception ->
            if (exception is CancellationException) {
                throw exception
            }
            unavailableResult()
        }
    }

    private fun mapResponse(
        response: HttpResponse,
        requestedUrl: String,
    ): UrlMetadataResult {
        val contentType =
            response.headers[HttpHeaders.ContentType]
                ?.substringBefore(';')
                ?.trim()
        val contentDisposition = response.headers[HttpHeaders.ContentDisposition]?.trim()
        val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
        val finalUrl =
            response.call.request.url
                .toString()
        val fileName = extractFileName(contentDisposition, contentType, finalUrl)
        val fileExtension = extractFileExtension(fileName, finalUrl, contentType)
        val fileType = detectFileType(fileName, fileExtension, contentType)
        val isLikelyDownload =
            detectLikelyDownload(
                contentDisposition = contentDisposition,
                fileName = fileName,
                fileExtension = fileExtension,
                contentType = contentType,
                fileType = fileType,
            )

        return UrlMetadataResult(
            status = UrlMetadataStatus.Available,
            finalUrl = finalUrl.takeIf { hasMeaningfulFinalUrl(requestedUrl, it) },
            contentType = contentType,
            contentDisposition = contentDisposition,
            contentLength = contentLength,
            fileName = fileName,
            fileExtension = fileExtension,
            fileType = fileType,
            isLikelyDownload = isLikelyDownload,
            reasons = emptyList(),
        )
    }

    private fun detectLikelyDownload(
        contentDisposition: String?,
        fileName: String?,
        fileExtension: String?,
        contentType: String?,
        fileType: DownloadFileType,
    ): Boolean {
        val lowerContentDisposition = contentDisposition.orEmpty().lowercase()
        val lowerContentType = contentType.orEmpty().lowercase()
        return lowerContentDisposition.contains("attachment") ||
            lowerContentDisposition.contains("filename=") ||
            lowerContentDisposition.contains("filename*=") ||
            lowerContentType == "application/octet-stream" ||
            fileType in
            setOf(
                DownloadFileType.Pdf,
                DownloadFileType.Document,
                DownloadFileType.Spreadsheet,
                DownloadFileType.Presentation,
                DownloadFileType.Archive,
                DownloadFileType.AndroidApp,
                DownloadFileType.AppleDiskImage,
                DownloadFileType.WindowsExecutable,
                DownloadFileType.Script,
            )
    }

    private fun extractFileName(
        contentDisposition: String?,
        contentType: String?,
        finalUrl: String,
    ): String? {
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
        if (!filename.isNullOrBlank()) {
            return filename
        }

        if (isWebPageContentType(contentType)) {
            return null
        }

        val parsedUrl = parseUrl(finalUrl)
        val lastSegment = parsedUrl.path.substringAfterLast('/')
        if (!isFileLikeSegment(lastSegment)) {
            return null
        }

        return lastSegment
    }

    private fun extractFileExtension(
        fileName: String?,
        finalUrl: String,
        contentType: String?,
    ): String? {
        if (isWebPageContentType(contentType)) {
            return null
        }

        val candidate =
            fileName ?: parseUrl(finalUrl).path
                .substringAfterLast('/')

        val extension = candidate.substringAfterLast('.', "")
        return extension.takeIf { it.isNotBlank() && it != candidate }
    }

    private fun detectFileType(
        fileName: String?,
        fileExtension: String?,
        contentType: String?,
    ): DownloadFileType {
        val normalizedExtension = fileExtension.orEmpty().lowercase()
        val normalizedContentType = contentType.orEmpty().lowercase()
        return when {
            normalizedExtension == "pdf" || normalizedContentType == "application/pdf" -> DownloadFileType.Pdf

            normalizedExtension in setOf("doc", "docx", "odt", "rtf", "txt") -> DownloadFileType.Document

            normalizedExtension in setOf("xls", "xlsx", "csv", "ods") -> DownloadFileType.Spreadsheet

            normalizedExtension in setOf("ppt", "pptx", "odp") -> DownloadFileType.Presentation

            normalizedExtension in setOf("jpg", "jpeg", "png", "gif", "webp", "svg", "heic") -> DownloadFileType.Image

            normalizedExtension in setOf("mp3", "wav", "aac", "ogg", "m4a") -> DownloadFileType.Audio

            normalizedExtension in setOf("mp4", "mov", "avi", "mkv", "webm") -> DownloadFileType.Video

            normalizedExtension in setOf("zip", "rar", "7z", "tar", "gz") -> DownloadFileType.Archive

            normalizedExtension in
                setOf(
                    "apk",
                    "aab",
                ) || normalizedContentType == "application/vnd.android.package-archive" -> DownloadFileType.AndroidApp

            normalizedExtension in setOf("dmg", "pkg") -> DownloadFileType.AppleDiskImage

            normalizedExtension in setOf("exe", "msi", "scr") -> DownloadFileType.WindowsExecutable

            normalizedExtension in setOf("js", "jar", "bat", "cmd", "sh", "ps1", "vbs") -> DownloadFileType.Script

            else -> DownloadFileType.Unknown
        }
    }

    private fun isWebPageContentType(contentType: String?): Boolean {
        val normalized = contentType.orEmpty().lowercase()
        return normalized == "text/html" || normalized == "application/xhtml+xml"
    }

    private fun isFileLikeSegment(segment: String): Boolean {
        if (segment.isBlank()) {
            return false
        }

        val extension = segment.substringAfterLast('.', "")
        if (extension.isBlank() || extension == segment) {
            return false
        }

        return extension.lowercase() !in WEB_PAGE_EXTENSIONS
    }

    private fun unavailableResult(): UrlMetadataResult = UrlMetadataResult(
        status = UrlMetadataStatus.Unavailable,
        finalUrl = null,
        contentType = null,
        contentDisposition = null,
        contentLength = null,
        fileName = null,
        fileExtension = null,
        fileType = DownloadFileType.Unknown,
        isLikelyDownload = false,
        reasons = listOf("Destination metadata could not be checked."),
    )

    private fun hasMeaningfulFinalUrl(
        requestedUrl: String,
        finalUrl: String,
    ): Boolean {
        if (finalUrl.isBlank()) {
            return false
        }

        val requested = parseUrl(requestedUrl)
        val resolved = parseUrl(finalUrl)

        if (requested.scheme != resolved.scheme || requested.host != resolved.host) {
            return true
        }

        if (normalizePath(requested.path) != normalizePath(resolved.path)) {
            return true
        }

        if (requested.query != resolved.query) {
            return true
        }

        if (requested.fragment != resolved.fragment) {
            return true
        }

        return false
    }

    private fun normalizePath(path: String): String {
        if (path.isBlank() || path == "/") {
            return ""
        }

        return path.trimEnd('/').ifBlank { "/" }
    }

    private companion object {
        val WEB_PAGE_EXTENSIONS =
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
    }
}

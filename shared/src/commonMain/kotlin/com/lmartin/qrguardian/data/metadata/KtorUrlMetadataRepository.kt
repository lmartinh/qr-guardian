package com.lmartin.qrguardian.data.metadata

import com.lmartin.qrguardian.domain.metadata.DownloadFileType
import com.lmartin.qrguardian.domain.metadata.normalizeUrlForRequest
import com.lmartin.qrguardian.domain.metadata.UrlMetadataRepository
import com.lmartin.qrguardian.domain.metadata.UrlMetadataResult
import com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus
import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException

class KtorUrlMetadataRepository(
    private val httpClient: HttpClient
) : UrlMetadataRepository {
    override suspend fun fetchMetadata(url: String): UrlMetadataResult {
        return runCatching {
            val response = httpClient.head(normalizeUrlForRequest(url))
            if (response.status.value !in 200..299 || response.status == HttpStatusCode.MethodNotAllowed) {
                return unavailableResult()
            }

            mapResponse(response, url)
        }.getOrElse { exception ->
            if (exception is CancellationException) {
                throw exception
            }
            unavailableResult()
        }
    }

    private fun mapResponse(response: HttpResponse, requestedUrl: String): UrlMetadataResult {
        val contentType = response.headers[HttpHeaders.ContentType]
            ?.substringBefore(';')
            ?.trim()
        val contentDisposition = response.headers[HttpHeaders.ContentDisposition]?.trim()
        val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
        val finalUrl = response.call.request.url.toString()
        val fileName = extractFileName(contentDisposition, finalUrl)
        val fileExtension = extractFileExtension(fileName, finalUrl)
        val fileType = detectFileType(fileName, fileExtension, contentType)
        val isLikelyDownload = detectLikelyDownload(
            contentDisposition = contentDisposition,
            fileName = fileName,
            fileExtension = fileExtension,
            contentType = contentType,
            fileType = fileType
        )

        return UrlMetadataResult(
            status = UrlMetadataStatus.Available,
            finalUrl = finalUrl.takeIf { it.isNotBlank() && it != requestedUrl },
            contentType = contentType,
            contentDisposition = contentDisposition,
            contentLength = contentLength,
            fileName = fileName,
            fileExtension = fileExtension,
            fileType = fileType,
            isLikelyDownload = isLikelyDownload,
            reasons = emptyList()
        )
    }

    private fun detectLikelyDownload(
        contentDisposition: String?,
        fileName: String?,
        fileExtension: String?,
        contentType: String?,
        fileType: DownloadFileType
    ): Boolean {
        val lowerContentDisposition = contentDisposition.orEmpty().lowercase()
        val lowerContentType = contentType.orEmpty().lowercase()
        return lowerContentDisposition.contains("attachment") ||
            !fileName.isNullOrBlank() ||
            !fileExtension.isNullOrBlank() ||
            lowerContentType == "application/octet-stream" ||
            fileType != DownloadFileType.Unknown
    }

    private fun extractFileName(contentDisposition: String?, finalUrl: String): String? {
        val disposition = contentDisposition.orEmpty()
        val filenameStar = disposition
            .split(';')
            .firstOrNull { it.trim().startsWith("filename*=", ignoreCase = true) }
            ?.substringAfter('=')
            ?.trim()
            ?.trim('"')
        if (!filenameStar.isNullOrBlank()) {
            val value = filenameStar.substringAfter("''", filenameStar)
            return value.substringAfterLast('/')
        }

        val filename = disposition
            .split(';')
            .firstOrNull { it.trim().startsWith("filename=", ignoreCase = true) }
            ?.substringAfter('=')
            ?.trim()
            ?.trim('"')
        if (!filename.isNullOrBlank()) {
            return filename
        }

        val lastSegment = finalUrl
            .substringBefore('?')
            .substringBefore('#')
            .substringAfterLast('/')
        return lastSegment.takeIf { it.contains('.') }
    }

    private fun extractFileExtension(fileName: String?, finalUrl: String): String? {
        val candidate = fileName ?: finalUrl
            .substringBefore('?')
            .substringBefore('#')
            .substringAfterLast('/')

        val extension = candidate.substringAfterLast('.', "")
        return extension.takeIf { it.isNotBlank() && it != candidate }
    }

    private fun detectFileType(
        fileName: String?,
        fileExtension: String?,
        contentType: String?
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
            normalizedExtension in setOf("apk", "aab") || normalizedContentType == "application/vnd.android.package-archive" -> DownloadFileType.AndroidApp
            normalizedExtension in setOf("dmg", "pkg") -> DownloadFileType.AppleDiskImage
            normalizedExtension in setOf("exe", "msi", "scr") -> DownloadFileType.WindowsExecutable
            normalizedExtension in setOf("js", "jar", "bat", "cmd", "sh", "ps1", "vbs") -> DownloadFileType.Script
            else -> DownloadFileType.Unknown
        }
    }

    private fun unavailableResult(): UrlMetadataResult {
        return UrlMetadataResult(
            status = UrlMetadataStatus.Unavailable,
            finalUrl = null,
            contentType = null,
            contentDisposition = null,
            contentLength = null,
            fileName = null,
            fileExtension = null,
            fileType = DownloadFileType.Unknown,
            isLikelyDownload = false,
            reasons = listOf("Destination metadata could not be checked.")
        )
    }
}

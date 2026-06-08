package com.lmartin.qrguardian.data.metadata

import com.lmartin.qrguardian.domain.metadata.DownloadFileType
import com.lmartin.qrguardian.domain.metadata.UrlMetadataRepository
import com.lmartin.qrguardian.domain.metadata.UrlMetadataResult
import com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus
import com.lmartin.qrguardian.domain.metadata.UrlResourceKind
import com.lmartin.qrguardian.domain.metadata.inferFileExtensionFromName
import com.lmartin.qrguardian.domain.metadata.inferFileNameFromPath
import com.lmartin.qrguardian.domain.metadata.inferFileTypeFromExtension
import com.lmartin.qrguardian.domain.metadata.inferUrlResourceKind
import com.lmartin.qrguardian.domain.metadata.isAttachmentDisposition
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
        val path = parseUrl(finalUrl).path
        val fileName = inferFileNameFromPath(path, contentType, contentDisposition)
        val fileExtension = inferFileExtensionFromName(fileName)
        val fileType = inferFileTypeFromExtension(fileName, fileExtension, contentType)
        val resourceKind =
            inferUrlResourceKind(
                contentType = contentType,
                contentDisposition = contentDisposition,
                fileName = fileName,
                fileExtension = fileExtension,
                fileType = fileType,
            )
        val isLikelyDownload = detectLikelyDownload(contentDisposition, contentType, resourceKind)

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
            resourceKind = resourceKind,
        )
    }

    private fun detectLikelyDownload(
        contentDisposition: String?,
        contentType: String?,
        resourceKind: UrlResourceKind,
    ): Boolean {
        val lowerContentType = contentType.orEmpty().lowercase()
        return isAttachmentDisposition(contentDisposition) ||
            lowerContentType == "application/octet-stream" ||
            resourceKind in setOf(
                UrlResourceKind.Archive,
                UrlResourceKind.InstallerOrExecutable,
                UrlResourceKind.UnknownBinary,
            )
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
        resourceKind = UrlResourceKind.Unknown,
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
}

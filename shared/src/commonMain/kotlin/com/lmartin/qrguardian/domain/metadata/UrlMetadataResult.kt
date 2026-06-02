package com.lmartin.qrguardian.domain.metadata

data class UrlMetadataResult(
    val status: UrlMetadataStatus,
    val finalUrl: String?,
    val contentType: String?,
    val contentDisposition: String?,
    val contentLength: Long?,
    val fileName: String?,
    val fileExtension: String?,
    val fileType: DownloadFileType,
    val isLikelyDownload: Boolean,
    val reasons: List<String>
)

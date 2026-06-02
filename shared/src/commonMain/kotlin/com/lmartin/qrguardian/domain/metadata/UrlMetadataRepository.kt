package com.lmartin.qrguardian.domain.metadata

interface UrlMetadataRepository {
    suspend fun fetchMetadata(url: String): UrlMetadataResult
}

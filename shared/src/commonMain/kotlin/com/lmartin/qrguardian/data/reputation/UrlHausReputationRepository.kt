package com.lmartin.qrguardian.data.reputation

import com.lmartin.qrguardian.domain.reputation.ThreatCategory
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class UrlHausReputationRepository(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val json: Json =
        Json {
            ignoreUnknownKeys = true
        },
) : UrlReputationRepository {
    override suspend fun checkUrl(url: String): UrlReputationResult {
        if (apiKey.isBlank()) {
            return notConfiguredResult()
        }

        return runCatching {
            val response =
                httpClient.post(URLHAUS_LOOKUP_URL) {
                    header("Auth-Key", apiKey)
                    header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
                    setBody("url=${url.encodeURLParameter()}")
                }

            if (response.status.value !in 200..299) {
                return errorResult()
            }

            val responseBody = response.bodyAsText()
            val responseDto =
                json.decodeFromString(
                    UrlHausResponseDto.serializer(),
                    responseBody.ifBlank { "{}" },
                )

            mapResponse(responseDto)
        }.getOrElse {
            errorResult()
        }
    }

    private fun mapResponse(responseDto: UrlHausResponseDto): UrlReputationResult = when (responseDto.queryStatus?.lowercase()) {
        "no_results" -> {
            UrlReputationResult(
                status = UrlReputationStatus.Clean,
                provider = PROVIDER_NAME,
                categories = emptyList(),
                reasons = listOf("No threats were reported by URLhaus."),
            )
        }

        "ok" -> {
            when (responseDto.urlStatus?.lowercase()) {
                "online", "offline" -> {
                    UrlReputationResult(
                        status = UrlReputationStatus.Malicious,
                        provider = PROVIDER_NAME,
                        categories = listOf(ThreatCategory.Malware),
                        reasons = listOf("URLhaus reported this URL as malware-related."),
                    )
                }

                "unknown" -> {
                    unknownResult()
                }

                else -> {
                    unknownResult()
                }
            }
        }

        "invalid_url", "http_post_expected" -> {
            unknownResult()
        }

        else -> {
            unknownResult()
        }
    }

    private fun notConfiguredResult(): UrlReputationResult = UrlReputationResult(
        status = UrlReputationStatus.NotConfigured,
        provider = PROVIDER_NAME,
        categories = emptyList(),
        reasons = listOf("URLhaus API key is not configured."),
    )

    private fun errorResult(): UrlReputationResult = UrlReputationResult(
        status = UrlReputationStatus.Error,
        provider = PROVIDER_NAME,
        categories = emptyList(),
        reasons = listOf("URLhaus check is currently unavailable."),
    )

    private fun unknownResult(): UrlReputationResult = UrlReputationResult(
        status = UrlReputationStatus.Unknown,
        provider = PROVIDER_NAME,
        categories = listOf(ThreatCategory.Unknown),
        reasons = listOf("URLhaus returned an unknown result."),
    )

    @Serializable
    private data class UrlHausResponseDto(
        @SerialName("query_status")
        val queryStatus: String? = null,
        @SerialName("url_status")
        val urlStatus: String? = null,
    )

    private companion object {
        const val PROVIDER_NAME = "URLhaus"
        const val URLHAUS_LOOKUP_URL = "https://urlhaus-api.abuse.ch/v1/url/"
    }
}

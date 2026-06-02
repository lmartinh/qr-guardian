package com.lmartin.qrguardian.data.reputation.google

import com.lmartin.qrguardian.domain.reputation.ThreatCategory
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class GoogleSafeBrowsingUrlReputationRepository(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val clientId: String = "qr-guardian",
    private val clientVersion: String = "1.0",
    private val json: Json = Json {
        ignoreUnknownKeys = true
    }
) : UrlReputationRepository {
    override suspend fun checkUrl(url: String): UrlReputationResult {
        if (apiKey.isBlank()) {
            return notConfiguredResult()
        }

        return runCatching {
            val request = SafeBrowsingRequestDto(
                client = SafeBrowsingClientDto(
                    clientId = clientId,
                    clientVersion = clientVersion
                ),
                threatInfo = SafeBrowsingThreatInfoDto(
                    threatTypes = listOf(
                        "MALWARE",
                        "SOCIAL_ENGINEERING",
                        "UNWANTED_SOFTWARE",
                        "POTENTIALLY_HARMFUL_APPLICATION"
                    ),
                    platformTypes = listOf("ANY_PLATFORM"),
                    threatEntryTypes = listOf("URL"),
                    threatEntries = listOf(
                        SafeBrowsingThreatEntryDto(url = url)
                    )
                )
            )

            val response = httpClient.post(
                "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=$apiKey"
            ) {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.encodeToString(SafeBrowsingRequestDto.serializer(), request))
            }

            if (response.status.value !in 200..299) {
                return errorResult()
            }

            val responseText = response.bodyAsText()
            val responseDto = json.decodeFromString(
                SafeBrowsingResponseDto.serializer(),
                responseText.ifBlank { "{}" }
            )

            mapResponse(responseDto)
        }.getOrElse {
            errorResult()
        }
    }

    private fun mapResponse(responseDto: SafeBrowsingResponseDto): UrlReputationResult {
        val matches = responseDto.matches
        if (matches.isEmpty()) {
            return UrlReputationResult(
                status = UrlReputationStatus.Clean,
                provider = PROVIDER_NAME,
                categories = emptyList(),
                reasons = listOf("No threats were reported by Google Safe Browsing.")
            )
        }

        val categories = matches.map { match ->
            mapThreatCategory(match.threatType)
        }.distinct()

        val reasons = matches.mapNotNull { match ->
            match.threatType?.let { "Google Safe Browsing reported this URL as $it." }
        }.distinct()

        return UrlReputationResult(
            status = UrlReputationStatus.Malicious,
            provider = PROVIDER_NAME,
            categories = categories,
            reasons = reasons
        )
    }

    private fun mapThreatCategory(threatType: String?): ThreatCategory {
        return when (threatType) {
            "MALWARE" -> ThreatCategory.Malware
            "SOCIAL_ENGINEERING" -> ThreatCategory.Phishing
            "UNWANTED_SOFTWARE" -> ThreatCategory.UnwantedSoftware
            "POTENTIALLY_HARMFUL_APPLICATION" -> ThreatCategory.Malware
            else -> ThreatCategory.Unknown
        }
    }

    private fun notConfiguredResult(): UrlReputationResult {
        return UrlReputationResult(
            status = UrlReputationStatus.NotConfigured,
            provider = PROVIDER_NAME,
            categories = emptyList(),
            reasons = listOf("Google Safe Browsing API key is not configured.")
        )
    }

    private fun errorResult(): UrlReputationResult {
        return UrlReputationResult(
            status = UrlReputationStatus.Error,
            provider = PROVIDER_NAME,
            categories = emptyList(),
            reasons = listOf("Google Safe Browsing check is currently unavailable.")
        )
    }

    private companion object {
        const val PROVIDER_NAME = "Google Safe Browsing"
    }
}

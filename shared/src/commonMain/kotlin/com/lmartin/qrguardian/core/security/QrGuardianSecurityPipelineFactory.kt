package com.lmartin.qrguardian.core.security

import com.lmartin.qrguardian.data.metadata.KtorUrlMetadataRepository
import com.lmartin.qrguardian.data.reputation.RemoteReputationConfig
import com.lmartin.qrguardian.data.reputation.UrlReputationRepositoryFactory
import com.lmartin.qrguardian.domain.analyzer.DefaultLocalScanAnalyzer
import com.lmartin.qrguardian.domain.analyzer.LocalScanAnalyzer
import com.lmartin.qrguardian.domain.classifier.DefaultQrContentClassifier
import com.lmartin.qrguardian.domain.classifier.QrContentClassifier
import com.lmartin.qrguardian.domain.normalizer.DefaultQrTextNormalizer
import com.lmartin.qrguardian.domain.normalizer.QrTextNormalizer
import com.lmartin.qrguardian.domain.usecase.AnalyzeQrSafetyUseCase
import io.ktor.client.HttpClient

object QrGuardianSecurityPipelineFactory {
    fun create(
        httpClient: HttpClient,
        remoteReputationConfig: RemoteReputationConfig = RemoteReputationConfig(),
        textNormalizer: QrTextNormalizer = DefaultQrTextNormalizer(),
        contentClassifier: QrContentClassifier = DefaultQrContentClassifier(),
        localScanAnalyzer: LocalScanAnalyzer = DefaultLocalScanAnalyzer()
    ): AnalyzeQrSafetyUseCase {
        return AnalyzeQrSafetyUseCase(
            textNormalizer = textNormalizer,
            contentClassifier = contentClassifier,
            localScanAnalyzer = localScanAnalyzer,
            urlMetadataRepository = KtorUrlMetadataRepository(httpClient),
            urlReputationRepository = UrlReputationRepositoryFactory.create(
                config = remoteReputationConfig,
                httpClient = httpClient
            )
        )
    }

    @Deprecated(
        message = "Use create(httpClient, remoteReputationConfig) instead.",
        replaceWith = ReplaceWith("create(httpClient, remoteReputationConfig, textNormalizer, contentClassifier, localScanAnalyzer)")
    )
    fun createAnalyzeQrSafetyUseCase(
        httpClient: HttpClient,
        remoteReputationConfig: RemoteReputationConfig = RemoteReputationConfig(),
        textNormalizer: QrTextNormalizer = DefaultQrTextNormalizer(),
        contentClassifier: QrContentClassifier = DefaultQrContentClassifier(),
        localScanAnalyzer: LocalScanAnalyzer = DefaultLocalScanAnalyzer()
    ): AnalyzeQrSafetyUseCase {
        return create(
            httpClient = httpClient,
            remoteReputationConfig = remoteReputationConfig,
            textNormalizer = textNormalizer,
            contentClassifier = contentClassifier,
            localScanAnalyzer = localScanAnalyzer
        )
    }
}

package com.lmartin.qrguardian.di

import com.lmartin.qrguardian.data.metadata.KtorUrlMetadataRepository
import com.lmartin.qrguardian.data.reputation.RemoteReputationConfig
import com.lmartin.qrguardian.data.reputation.UrlReputationRepositoryFactory
import com.lmartin.qrguardian.domain.analyzer.DefaultLocalScanAnalyzer
import com.lmartin.qrguardian.domain.analyzer.LocalScanAnalyzer
import com.lmartin.qrguardian.domain.classifier.DefaultQrContentClassifier
import com.lmartin.qrguardian.domain.classifier.QrContentClassifier
import com.lmartin.qrguardian.domain.metadata.UrlMetadataRepository
import com.lmartin.qrguardian.domain.normalizer.DefaultQrTextNormalizer
import com.lmartin.qrguardian.domain.normalizer.QrTextNormalizer
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import com.lmartin.qrguardian.domain.usecase.AnalyzeQrSafetyUseCase
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

fun initKoin(
    remoteReputationConfig: RemoteReputationConfig = RemoteReputationConfig(),
    additionalModules: List<Module> = emptyList(),
): KoinApplication {
    return koinApplication {
        if (additionalModules.isNotEmpty()) {
            allowOverride(true)
        }

        modules(
            configurationModule(remoteReputationConfig),
            networkModule,
            securityModule,
            *additionalModules.toTypedArray(),
        )
    }
}

fun configurationModule(remoteReputationConfig: RemoteReputationConfig): Module {
    return module {
        single<RemoteReputationConfig> { remoteReputationConfig }
    }
}

val securityModule: Module = module {
    single<QrTextNormalizer> { DefaultQrTextNormalizer() }
    single<QrContentClassifier> { DefaultQrContentClassifier() }
    single<LocalScanAnalyzer> { DefaultLocalScanAnalyzer() }
    single<UrlMetadataRepository> { KtorUrlMetadataRepository(get()) }
    single<UrlReputationRepository> {
        UrlReputationRepositoryFactory.create(
            config = get(),
            httpClient = get(),
        )
    }
    single {
        AnalyzeQrSafetyUseCase(
            textNormalizer = get(),
            contentClassifier = get(),
            localScanAnalyzer = get(),
            urlMetadataRepository = get(),
            urlReputationRepository = get(),
        )
    }
}

expect val networkModule: Module

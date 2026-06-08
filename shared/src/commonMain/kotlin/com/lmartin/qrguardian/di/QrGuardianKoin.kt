package com.lmartin.qrguardian.di

import com.lmartin.qrguardian.core.security.QrGuardianSecurityPipelineFactory
import com.lmartin.qrguardian.data.reputation.RemoteReputationConfig
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

fun initKoin(
    remoteReputationConfig: RemoteReputationConfig = RemoteReputationConfig(),
    additionalModules: List<Module> = emptyList(),
): KoinApplication = koinApplication {
    if (additionalModules.isNotEmpty()) {
        allowOverride(true)
    }

    modules(
        module {
            single<RemoteReputationConfig> { remoteReputationConfig }
        },
        networkModule,
        module {
            single {
                QrGuardianSecurityPipelineFactory.create(
                    httpClient = get(),
                    remoteReputationConfig = get(),
                )
            }
        },
        *additionalModules.toTypedArray(),
    )
}

expect val networkModule: Module

package com.lmartin.qrguardian.di

import com.lmartin.qrguardian.core.network.QrGuardianHttpClientFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

actual val networkModule: Module = module {
    single<HttpClient> {
        QrGuardianHttpClientFactory.create(Darwin)
    }
}

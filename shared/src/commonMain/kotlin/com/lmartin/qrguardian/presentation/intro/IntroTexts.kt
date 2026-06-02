package com.lmartin.qrguardian.presentation.intro

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import qrguardian.shared.generated.resources.Res
import qrguardian.shared.generated.resources.intro_badge
import qrguardian.shared.generated.resources.intro_description
import qrguardian.shared.generated.resources.intro_start_scanning
import qrguardian.shared.generated.resources.intro_title_highlight
import qrguardian.shared.generated.resources.intro_title_main

@Composable
internal fun rememberIntroTexts(): IntroTexts {
    return IntroTexts(
        badge = stringResource(Res.string.intro_badge),
        titleMain = stringResource(Res.string.intro_title_main),
        titleHighlight = stringResource(Res.string.intro_title_highlight),
        description = stringResource(Res.string.intro_description),
        startScanning = stringResource(Res.string.intro_start_scanning),
    )
}

internal data class IntroTexts(
    val badge: String,
    val titleMain: String,
    val titleHighlight: String,
    val description: String,
    val startScanning: String,
)

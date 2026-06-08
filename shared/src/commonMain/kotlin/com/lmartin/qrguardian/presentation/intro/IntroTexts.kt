package com.lmartin.qrguardian.presentation.intro

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import qrguardian.shared.generated.resources.Res
import qrguardian.shared.generated.resources.intro_badge
import qrguardian.shared.generated.resources.intro_description
import qrguardian.shared.generated.resources.intro_permission_card_message
import qrguardian.shared.generated.resources.intro_permission_card_title
import qrguardian.shared.generated.resources.intro_permission_message
import qrguardian.shared.generated.resources.intro_permission_open_settings
import qrguardian.shared.generated.resources.intro_start_scanning
import qrguardian.shared.generated.resources.intro_title_highlight
import qrguardian.shared.generated.resources.intro_title_main

@Composable
internal fun rememberIntroTexts(): IntroTexts = IntroTexts(
    badge = stringResource(Res.string.intro_badge),
    titleMain = stringResource(Res.string.intro_title_main),
    titleHighlight = stringResource(Res.string.intro_title_highlight),
    description = stringResource(Res.string.intro_description),
    startScanning = stringResource(Res.string.intro_start_scanning),
    permissionMessage = stringResource(Res.string.intro_permission_message),
    permissionCardTitle = stringResource(Res.string.intro_permission_card_title),
    permissionCardMessage = stringResource(Res.string.intro_permission_card_message),
    permissionCardAction = stringResource(Res.string.intro_permission_open_settings),
)

internal data class IntroTexts(
    val badge: String,
    val titleMain: String,
    val titleHighlight: String,
    val description: String,
    val startScanning: String,
    val permissionMessage: String,
    val permissionCardTitle: String,
    val permissionCardMessage: String,
    val permissionCardAction: String,
)

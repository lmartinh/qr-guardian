package com.lmartin.qrguardian.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import qrguardian.shared.generated.resources.OutfitBold
import qrguardian.shared.generated.resources.OutfitMedium
import qrguardian.shared.generated.resources.OutfitRegular
import qrguardian.shared.generated.resources.OutfitSemiBold
import qrguardian.shared.generated.resources.Res

@Composable
fun qrGuardianTypography(): Typography {
    val outfitFontFamily = FontFamily(
        Font(Res.font.OutfitRegular, FontWeight.Normal),
        Font(Res.font.OutfitMedium, FontWeight.Medium),
        Font(Res.font.OutfitSemiBold, FontWeight.SemiBold),
        Font(Res.font.OutfitBold, FontWeight.Bold),
    )

    return Typography(
        displayLarge = TextStyle(
            fontFamily = outfitFontFamily,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        displayMedium = TextStyle(
            fontFamily = outfitFontFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        displaySmall = TextStyle(
            fontFamily = outfitFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        bodyLarge = TextStyle(
            fontFamily = outfitFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
        ),
        bodyMedium = TextStyle(
            fontFamily = outfitFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
        ),
        bodySmall = TextStyle(
            fontFamily = outfitFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
        ),
        labelLarge = TextStyle(
            fontFamily = outfitFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        ),
        labelSmall = TextStyle(
            fontFamily = outfitFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        ),
    )
}

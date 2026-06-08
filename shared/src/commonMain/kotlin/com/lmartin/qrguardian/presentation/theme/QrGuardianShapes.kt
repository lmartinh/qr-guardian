package com.lmartin.qrguardian.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val QrGuardianShapes =
    Shapes(
        small = RoundedCornerShape(QrGuardianRadius.Small),
        medium = RoundedCornerShape(QrGuardianRadius.Medium),
        large = RoundedCornerShape(QrGuardianRadius.Large),
    )

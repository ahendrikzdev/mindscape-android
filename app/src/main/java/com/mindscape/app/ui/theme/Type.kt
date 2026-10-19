package com.mindscape.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mindscape.app.R

val JakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_bold, FontWeight.Bold),
)

private fun jakarta(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
): TextStyle = TextStyle(
    fontFamily = JakartaSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
)

val MindscapeTypography = Typography(
    displayLarge = jakarta(57, 64),
    displayMedium = jakarta(45, 52),
    displaySmall = jakarta(36, 44),
    headlineLarge = jakarta(32, 40),
    headlineMedium = jakarta(28, 36),
    headlineSmall = jakarta(24, 32),
    titleLarge = jakarta(22, 28, FontWeight.Medium),
    titleMedium = jakarta(16, 24, FontWeight.Medium),
    titleSmall = jakarta(14, 20, FontWeight.Medium),
    bodyLarge = jakarta(16, 24),
    bodyMedium = jakarta(14, 20),
    bodySmall = jakarta(12, 16),
    labelLarge = jakarta(14, 20, FontWeight.Medium),
    labelMedium = jakarta(12, 16, FontWeight.Medium),
    labelSmall = jakarta(11, 16, FontWeight.Medium),
)

package com.betterdo.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography. The design pairs an italic display serif (Instrument Serif) with a
 * Chinese body sans (Noto Sans SC). Until those TTFs are bundled in `res/font`,
 * we fall back to the platform serif (which renders CJK) and the system sans so
 * the build stays green. To use the real faces, drop the OFL TTFs into
 * `res/font` and replace the two families below, e.g.:
 *
 *   val DisplaySerif = FontFamily(Font(R.font.instrument_serif_italic, style = FontStyle.Italic))
 *   val BodySans = FontFamily(Font(R.font.noto_sans_sc))
 */
val DisplaySerif = FontFamily.Serif
val BodySans = FontFamily.Default

/** Heading style helper — italic serif, the app's signature voice. */
val DisplayStyle = TextStyle(
    fontFamily = DisplaySerif,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Normal,
)

val BetterDoTypography = Typography(
    displayLarge = DisplayStyle.copy(fontSize = 40.sp, lineHeight = 44.sp),
    displayMedium = DisplayStyle.copy(fontSize = 32.sp, lineHeight = 36.sp),
    headlineMedium = DisplayStyle.copy(fontSize = 26.sp, lineHeight = 30.sp),
    headlineSmall = DisplayStyle.copy(fontSize = 22.sp, lineHeight = 27.sp),
    titleLarge = TextStyle(fontFamily = BodySans, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 25.sp),
    titleMedium = TextStyle(fontFamily = BodySans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = BodySans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = BodySans, fontWeight = FontWeight.Normal, fontSize = 13.5.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = BodySans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = BodySans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = BodySans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 15.sp),
    labelSmall = TextStyle(fontFamily = BodySans, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
)

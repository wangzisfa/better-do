package com.betterdo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.ThemeMode

val LocalBdColors = staticCompositionLocalOf { lightColors() }
val LocalAgentTone = staticCompositionLocalOf { AgentTone.COACH }
val LocalAccent = staticCompositionLocalOf { AccentEmber }

/** Accessor for BetterDo's design tokens, mirroring the `MaterialTheme.*` pattern. */
object BdTheme {
    val colors: BetterDoColors
        @Composable @ReadOnlyComposable get() = LocalBdColors.current
    val tone: AgentTone
        @Composable @ReadOnlyComposable get() = LocalAgentTone.current
    val accent: Color
        @Composable @ReadOnlyComposable get() = LocalAccent.current
}

@Composable
fun BetterDoTheme(
    tone: AgentTone = AgentTone.COACH,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accent: Color = AccentEmber,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val bd = if (dark) darkColors(accent) else lightColors(accent)

    val scheme = if (dark) {
        darkColorScheme(
            primary = accent,
            onPrimary = Color.White,
            secondary = accent,
            background = bd.paper,
            onBackground = bd.ink,
            surface = bd.surface,
            onSurface = bd.ink,
            surfaceVariant = bd.surface2,
            onSurfaceVariant = bd.inkSoft,
            outline = bd.inkFaint,
            outlineVariant = bd.line,
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            secondary = accent,
            background = bd.paper,
            onBackground = bd.ink,
            surface = bd.surface,
            onSurface = bd.ink,
            surfaceVariant = bd.surface2,
            onSurfaceVariant = bd.inkSoft,
            outline = bd.inkFaint,
            outlineVariant = bd.line,
        )
    }

    CompositionLocalProvider(
        LocalBdColors provides bd,
        LocalAgentTone provides tone,
        LocalAccent provides accent,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = BetterDoTypography,
            shapes = BetterDoShapes,
            content = content,
        )
    }
}

package com.betterdo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.betterdo.app.data.prefs.Settings
import com.betterdo.app.ui.nav.AppNav
import com.betterdo.app.ui.theme.BetterDoTheme
import com.betterdo.app.ui.theme.accentColor

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as BetterDoApplication).container

        setContent {
            val settings by container.settingsRepository.settings.collectAsState(initial = Settings())
            BetterDoTheme(
                tone = settings.tone,
                themeMode = settings.themeMode,
                accent = accentColor(settings.accentArgb),
            ) {
                AppNav(container = container, settings = settings)
            }
        }
    }
}

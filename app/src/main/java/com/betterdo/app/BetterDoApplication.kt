package com.betterdo.app

import android.app.Application
import com.betterdo.app.di.AppContainer
import com.betterdo.app.notifications.NotificationHelper

class BetterDoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.ensureChannel(this)
        // The day's todos come from onboarding: the user's own parsed list, or the
        // curated sample day if they tap "看看示例" (see ImportViewModel).
    }
}

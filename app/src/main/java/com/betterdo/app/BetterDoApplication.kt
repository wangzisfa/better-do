package com.betterdo.app

import android.app.Application
import com.betterdo.app.di.AppContainer
import com.betterdo.app.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BetterDoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.ensureChannel(this)
        // Seed the sample day on first launch so the designed experience is visible.
        CoroutineScope(Dispatchers.IO).launch {
            container.todoRepository.ensureSeeded()
        }
    }
}

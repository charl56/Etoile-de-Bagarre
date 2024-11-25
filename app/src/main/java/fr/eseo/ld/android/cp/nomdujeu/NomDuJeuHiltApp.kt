package fr.eseo.ld.android.cp.nomdujeu

import NotificationWorker
import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class NomDuJeuHiltApp : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleDailyNotification()
    }

    private fun scheduleDailyNotification() {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.HOURS)
            .setInitialDelay(20, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_notification",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

}
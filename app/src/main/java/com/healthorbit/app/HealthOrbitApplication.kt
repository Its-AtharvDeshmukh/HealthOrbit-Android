package com.healthorbit.app

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.healthorbit.app.di.AppContainer
import com.healthorbit.app.worker.WearableSyncWorker
import java.util.concurrent.TimeUnit

class HealthOrbitApplication : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(applicationContext)
    }

    fun scheduleWearableSync() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<WearableSyncWorker>(30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 2, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                WearableSyncWorker.UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelWearableSync() {
        try {
            WorkManager.getInstance(applicationContext).cancelUniqueWork(WearableSyncWorker.UNIQUE_WORK_NAME)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
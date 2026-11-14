package com.example.overlay

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.AtharPreferences
import java.util.concurrent.TimeUnit

/**
 * Periodic Watchdog Worker scheduled every 15 minutes.
 * Ensures UnlockOverlayService is continuously alive and restarts it immediately
 * if aggressive OEM battery management (such as Transsion XOS / HiOS PhoneMaster) kills the process.
 */
class ServiceWatchdogWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val context = applicationContext
            val prefs = AtharPreferences(context)

            if (prefs.overlayEnabled.value && UnlockOverlayManager.canDrawOverlays(context)) {
                UnlockOverlayService.startService(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "athar_service_watchdog_worker"

        fun scheduleWatchdog(context: Context) {
            try {
                val workRequest = PeriodicWorkRequestBuilder<ServiceWatchdogWorker>(
                    15, TimeUnit.MINUTES,
                    5, TimeUnit.MINUTES
                ).build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun cancelWatchdog(context: Context) {
            try {
                WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

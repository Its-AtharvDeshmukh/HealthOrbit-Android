package com.healthorbit.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.healthorbit.app.HealthOrbitApplication
import com.healthorbit.app.data.remote.WearableSyncRequest
import retrofit2.HttpException
import java.io.IOException

class WearableSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val UNIQUE_WORK_NAME = "HealthOrbit_WearableSyncWorker"
        private const val BATCH_SIZE = 100
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as? HealthOrbitApplication ?: return Result.failure()
        val container = app.appContainer
        val sessionManager = container.sessionManager
        val healthConnectManager = container.healthConnectManager
        val apiService = container.apiService

        // 1. Authenticated Session Check
        val token = sessionManager.getToken()
        if (token.isNullOrBlank()) {
            sessionManager.setLastWearableSyncAttempt("FAILED_NO_AUTH")
            return Result.failure()
        }

        // 2. Health Connect Availability Check
        if (!healthConnectManager.isAvailable()) {
            sessionManager.setLastWearableSyncAttempt("FAILED_HC_UNAVAILABLE")
            return Result.failure()
        }

        // 3. Health Connect Permission Check
        if (!healthConnectManager.hasPermissions()) {
            sessionManager.setLastWearableSyncAttempt("FAILED_PERMISSIONS_MISSING")
            return Result.failure()
        }

        // 4. Load Cursor and Query Bounded Metrics
        val lastSyncCursor = sessionManager.getLastSuccessfulWearableSyncAt()
        val records = try {
            healthConnectManager.readVitalsAndActivity(lastSyncCursor)
        } catch (e: Exception) {
            sessionManager.setLastWearableSyncAttempt("FAILED_HC_READ_ERROR")
            return Result.retry()
        }

        // 5. Zero Records Check
        if (records.isEmpty()) {
            sessionManager.setLastWearableSyncAttempt("SUCCESS_NO_NEW_DATA")
            return Result.success()
        }

        // 6. Upload in Bounded Batches (<= 100 records per HTTP POST)
        val batches = records.chunked(BATCH_SIZE)
        val uploadTimestamp = System.currentTimeMillis()

        try {
            for (batch in batches) {
                val request = WearableSyncRequest(
                    provider = "health_connect",
                    records = batch
                )
                val response = apiService.syncWearables(request)

                if (!response.isSuccessful) {
                    val code = response.code()
                    when (code) {
                        401 -> {
                            // Expired or invalid token: Purge session and abort
                            sessionManager.clearSession()
                            sessionManager.setLastWearableSyncAttempt("FAILED_AUTH_EXPIRED")
                            return Result.failure()
                        }
                        in 500..599 -> {
                            sessionManager.setLastWearableSyncAttempt("RETRYING_SERVER_ERROR")
                            return Result.retry()
                        }
                        else -> {
                            sessionManager.setLastWearableSyncAttempt("FAILED_HTTP_$code")
                            return Result.failure()
                        }
                    }
                }
            }

            // 7. Authoritative Cursor Update: ONLY after ALL batches succeed
            sessionManager.setLastSuccessfulWearableSyncAt(uploadTimestamp)
            return Result.success()

        } catch (e: IOException) {
            // Network loss, DNS failure, or connection timeout
            sessionManager.setLastWearableSyncAttempt("RETRYING_NETWORK_TIMEOUT")
            return Result.retry()
        } catch (e: HttpException) {
            if (e.code() == 401) {
                sessionManager.clearSession()
                sessionManager.setLastWearableSyncAttempt("FAILED_AUTH_EXPIRED")
                return Result.failure()
            }
            sessionManager.setLastWearableSyncAttempt("RETRYING_HTTP_ERROR")
            return Result.retry()
        } catch (e: Exception) {
            sessionManager.setLastWearableSyncAttempt("FAILED_UNHANDLED_EXCEPTION")
            return Result.failure()
        }
    }
}
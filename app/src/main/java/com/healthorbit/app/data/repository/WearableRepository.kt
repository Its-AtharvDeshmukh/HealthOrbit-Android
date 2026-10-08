package com.healthorbit.app.data.repository

import com.healthorbit.app.data.healthconnect.HealthConnectManager
import com.healthorbit.app.data.local.SessionManager
import com.healthorbit.app.data.remote.HealthOrbitApiService
import com.healthorbit.app.data.remote.HistoricalRecordDto
import com.healthorbit.app.data.remote.WearableRecordDto
import com.healthorbit.app.data.remote.WearableSyncRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WearableRepository(
    private val apiService: HealthOrbitApiService,
    private val healthConnectManager: HealthConnectManager,
    private val sessionManager: SessionManager
) {

    private val _wearableRecords = MutableStateFlow<List<WearableRecordDto>>(emptyList())
    val wearableRecords: StateFlow<List<WearableRecordDto>> = _wearableRecords.asStateFlow()

    private val _syncState = MutableStateFlow("IDLE")
    val syncState: StateFlow<String> = _syncState.asStateFlow()

    fun isHealthConnectAvailable(): Boolean = healthConnectManager.isAvailable()

    fun getRequiredPermissions(): Set<String> = healthConnectManager.requiredPermissions

    suspend fun hasPermissions(): Boolean = healthConnectManager.hasPermissions()

    suspend fun fetchLocalSamples(): List<WearableRecordDto> {
        val lastCursor = sessionManager.getLastSuccessfulWearableSyncAt()
        val records = healthConnectManager.readVitalsAndActivity(lastCursor)
        _wearableRecords.value = records
        return records
    }

    suspend fun syncWearableData(): Result<Int> {
        return try {
            _syncState.value = "SYNCING"

            if (!healthConnectManager.hasPermissions()) {
                _syncState.value = "PERMISSIONS_MISSING"
                return Result.failure(Exception("Health Connect permissions missing"))
            }

            val lastCursor = sessionManager.getLastSuccessfulWearableSyncAt()
            val records = healthConnectManager.readVitalsAndActivity(lastCursor)

            if (records.isEmpty()) {
                _syncState.value = "UP_TO_DATE"
                return Result.success(0)
            }

            val syncTimestamp = System.currentTimeMillis()
            val batches = records.chunked(100)
            var totalSynced = 0

            for (batch in batches) {
                val request = WearableSyncRequest(
                    provider = "health_connect",
                    records = batch
                )
                val response = apiService.syncWearables(request)

                if (!response.isSuccessful) {
                    _syncState.value = "FAILED"
                    return Result.failure(Exception("Sync HTTP Error ${response.code()}"))
                }

                val body = response.body()
                val count = body?.synced ?: body?.inserted ?: batch.size
                totalSynced += count
            }

            sessionManager.setLastSuccessfulWearableSyncAt(syncTimestamp)
            _wearableRecords.value = records
            _syncState.value = "SUCCESS"
            Result.success(totalSynced)
        } catch (e: Exception) {
            _syncState.value = "FAILED"
            Result.failure(e)
        }
    }

    suspend fun fetchMetricHistory(metricKey: String): Result<List<HistoricalRecordDto>> {
        return try {
            val response = apiService.getMetricHistory(metricKey)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.records ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
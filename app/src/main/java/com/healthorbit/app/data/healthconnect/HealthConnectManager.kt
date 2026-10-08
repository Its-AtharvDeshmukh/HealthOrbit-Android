package com.healthorbit.app.data.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.healthorbit.app.data.remote.WearableRecordDto
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {

    private val healthConnectClient: HealthConnectClient? by lazy {
        try {
            if (isAvailable()) {
                HealthConnectClient.getOrCreate(context)
            } else {
                null
            }
        } catch (e: Throwable) {
            null // Fallback safely to prevent app startup crash
        }
    }

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class)
    )

    fun isAvailable(): Boolean {
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Throwable) {
            false
        }
    }

    suspend fun hasPermissions(): Boolean {
        return try {
            val client = healthConnectClient ?: return false
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(requiredPermissions)
        } catch (e: Throwable) {
            false
        }
    }

    suspend fun readVitalsAndActivity(lastSuccessfulSyncEpochMillis: Long?): List<WearableRecordDto> {
        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()
        val startOfToday = LocalDate.now().atStartOfDay(zoneId).toInstant()

        val discreteStartTime = if (lastSuccessfulSyncEpochMillis != null && lastSuccessfulSyncEpochMillis > 0L) {
            Instant.ofEpochMilli(lastSuccessfulSyncEpochMillis).minus(5, ChronoUnit.MINUTES)
        } else {
            now.minus(24, ChronoUnit.HOURS)
        }

        val sleepStartTime = if (lastSuccessfulSyncEpochMillis != null && lastSuccessfulSyncEpochMillis > 0L) {
            discreteStartTime
        } else {
            now.minus(48, ChronoUnit.HOURS)
        }

        val discreteRecords = readDiscreteRecords(discreteStartTime, sleepStartTime, now)
        val cumulativeRecords = readDailyCumulativeRecords(startOfToday, now)

        return discreteRecords + cumulativeRecords
    }

    suspend fun readDiscreteRecords(
        startTime: Instant,
        sleepStartTime: Instant,
        endTime: Instant
    ): List<WearableRecordDto> {
        val client = healthConnectClient ?: return emptyList()
        val resultList = mutableListOf<WearableRecordDto>()
        val timeFilter = TimeRangeFilter.between(startTime, endTime)

        // 1. HEART RATE
        try {
            val hrRequest = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = timeFilter,
                ascendingOrder = false,
                pageSize = 10
            )
            val response = client.readRecords(hrRequest)
            for (record in response.records) {
                val sample = record.samples.lastOrNull() ?: continue
                resultList.add(
                    WearableRecordDto(
                        externalRecordId = "${record.metadata.id}_${sample.time.epochSecond}",
                        metric = "heart_rate",
                        value = sample.beatsPerMinute.toDouble(),
                        unit = "bpm",
                        recordedAt = sample.time.toString()
                    )
                )
            }
        } catch (_: Exception) {}

        // 2. BLOOD PRESSURE
        try {
            val bpResponse = client.readRecords(ReadRecordsRequest(BloodPressureRecord::class, timeFilter))
            for (record in bpResponse.records) {
                resultList.add(
                    WearableRecordDto(
                        externalRecordId = "${record.metadata.id}_sys",
                        metric = "blood_pressure_systolic",
                        value = record.systolic.inMillimetersOfMercury,
                        unit = "mmHg",
                        recordedAt = record.time.toString()
                    )
                )
                resultList.add(
                    WearableRecordDto(
                        externalRecordId = "${record.metadata.id}_dia",
                        metric = "blood_pressure_diastolic",
                        value = record.diastolic.inMillimetersOfMercury,
                        unit = "mmHg",
                        recordedAt = record.time.toString()
                    )
                )
            }
        } catch (_: Exception) {}

        // 3. SpO2
        try {
            val spo2Response = client.readRecords(ReadRecordsRequest(OxygenSaturationRecord::class, timeFilter))
            for (record in spo2Response.records) {
                resultList.add(
                    WearableRecordDto(
                        externalRecordId = record.metadata.id,
                        metric = "spo2",
                        value = record.percentage.value,
                        unit = "%",
                        recordedAt = record.time.toString()
                    )
                )
            }
        } catch (_: Exception) {}

        // 4. SLEEP SESSIONS
        try {
            val sleepFilter = TimeRangeFilter.between(sleepStartTime, endTime)
            val sleepResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = sleepFilter,
                    ascendingOrder = false
                )
            )
            for (record in sleepResponse.records) {
                val totalMinutes = Duration.between(record.startTime, record.endTime).toMinutes()
                val totalHours = Math.round((totalMinutes / 60.0) * 10.0) / 10.0
                resultList.add(
                    WearableRecordDto(
                        externalRecordId = record.metadata.id,
                        metric = "sleep_duration",
                        value = totalHours,
                        unit = "hours",
                        recordedAt = record.endTime.toString()
                    )
                )

                var deepMinutes = 0L
                for (stage in record.stages) {
                    if (stage.stage == SleepSessionRecord.STAGE_TYPE_DEEP) {
                        deepMinutes += Duration.between(stage.startTime, stage.endTime).toMinutes()
                    }
                }
                if (deepMinutes > 0) {
                    val deepHours = Math.round((deepMinutes / 60.0) * 10.0) / 10.0
                    resultList.add(
                        WearableRecordDto(
                            externalRecordId = "${record.metadata.id}_deep",
                            metric = "deep_sleep",
                            value = deepHours,
                            unit = "hours",
                            recordedAt = record.endTime.toString()
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // 5. BODY TEMPERATURE
        try {
            val tempResponse = client.readRecords(ReadRecordsRequest(BodyTemperatureRecord::class, timeFilter))
            for (record in tempResponse.records) {
                resultList.add(
                    WearableRecordDto(
                        externalRecordId = record.metadata.id,
                        metric = "body_temperature",
                        value = (Math.round(record.temperature.inCelsius * 10.0) / 10.0),
                        unit = "°C",
                        recordedAt = record.time.toString()
                    )
                )
            }
        } catch (_: Exception) {}

        // 6. HRV / STRESS
        try {
            val hrvResponse = client.readRecords(ReadRecordsRequest(HeartRateVariabilityRmssdRecord::class, timeFilter))
            for (record in hrvResponse.records) {
                resultList.add(
                    WearableRecordDto(
                        externalRecordId = record.metadata.id,
                        metric = "stress_hrv",
                        value = record.heartRateVariabilityMillis,
                        unit = "ms",
                        recordedAt = record.time.toString()
                    )
                )
            }
        } catch (_: Exception) {}

        return resultList
    }

    suspend fun readDailyCumulativeRecords(
        dayStart: Instant,
        endTime: Instant
    ): List<WearableRecordDto> {
        val client = healthConnectClient ?: return emptyList()
        val resultList = mutableListOf<WearableRecordDto>()
        val timeFilter = TimeRangeFilter.between(dayStart, endTime)
        val todayDate = LocalDate.now()

        // 1. STEPS
        try {
            val stepAggregate = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = timeFilter
                )
            )
            val totalSteps = stepAggregate[StepsRecord.COUNT_TOTAL] ?: 0L
            if (totalSteps > 0) {
                resultList.add(
                    WearableRecordDto(
                        externalRecordId = "steps_${todayDate}",
                        metric = "steps",
                        value = totalSteps.toDouble(),
                        unit = "steps",
                        recordedAt = endTime.toString()
                    )
                )
            }
        } catch (_: Exception) {}

        // 2. DISTANCE
        try {
            val distAggregate = client.aggregate(
                AggregateRequest(
                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                    timeRangeFilter = timeFilter
                )
            )
            val totalMeters = distAggregate[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0
            if (totalMeters > 0.0) {
                val km = totalMeters / 1000.0
                val roundedKm = Math.round(km * 100.0) / 100.0
                resultList.add(
                    WearableRecordDto(
                        externalRecordId = "distance_${todayDate}",
                        metric = "distance",
                        value = roundedKm,
                        unit = "km",
                        recordedAt = endTime.toString()
                    )
                )
            }
        } catch (_: Exception) {}

        // 3. CALORIES
        try {
            val calAggregate = client.aggregate(
                AggregateRequest(
                    metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                    timeRangeFilter = timeFilter
                )
            )
            val totalCalories = calAggregate[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0
            if (totalCalories > 0.0) {
                resultList.add(
                    WearableRecordDto(
                        externalRecordId = "calories_${todayDate}",
                        metric = "calories",
                        value = Math.round(totalCalories).toDouble(),
                        unit = "kcal",
                        recordedAt = endTime.toString()
                    )
                )
            }
        } catch (_: Exception) {}

        return resultList
    }
}
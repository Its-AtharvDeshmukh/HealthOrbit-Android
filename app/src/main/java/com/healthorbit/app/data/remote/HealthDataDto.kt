package com.healthorbit.app.data.remote

import com.google.gson.annotations.SerializedName

data class WearableRecordDto(
    @SerializedName("externalRecordId")
    val externalRecordId: String,
    @SerializedName("metric")
    val metric: String,
    @SerializedName("value")
    val value: Double,
    @SerializedName("unit")
    val unit: String,
    @SerializedName("recordedAt")
    val recordedAt: String
)

data class WearableSyncRequest(
    @SerializedName("provider")
    val provider: String = "health_connect",
    @SerializedName("records")
    val records: List<WearableRecordDto>
)

data class WearableSyncResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("inserted")
    val inserted: Int? = 0,
    @SerializedName("skipped")
    val skipped: Int? = 0,
    @SerializedName("synced")
    val synced: Int? = 0
)

data class HistoricalRecordDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("metricKey")
    val metricKey: String,
    @SerializedName("value")
    val value: Double,
    @SerializedName("unit")
    val unit: String?,
    @SerializedName("recordedAt")
    val recordedAt: String
)

data class MetricHistoryResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("metricKey")
    val metricKey: String,
    @SerializedName("unit")
    val unit: String,
    @SerializedName("records")
    val records: List<HistoricalRecordDto>
)
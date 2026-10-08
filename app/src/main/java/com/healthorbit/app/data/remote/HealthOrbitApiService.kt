package com.healthorbit.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HealthOrbitApiService {

    @POST("api/auth/mobile-login")
    suspend fun loginMobile(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("api/auth/google-mobile")
    suspend fun googleMobileAuth(
        @Body request: GoogleMobileAuthRequest
    ): Response<AuthResponse>

    @POST("api/wearables/sync")
    suspend fun syncWearables(
        @Body request: WearableSyncRequest
    ): Response<WearableSyncResponse>

    @GET("api/wearables/history/{metricKey}")
    suspend fun getMetricHistory(
        @Path("metricKey") metricKey: String,
        @Query("limit") limit: Int = 30
    ): Response<MetricHistoryResponse>
}
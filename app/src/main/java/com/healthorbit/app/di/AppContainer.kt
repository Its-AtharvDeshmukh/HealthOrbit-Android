package com.healthorbit.app.di

import android.content.Context
import com.healthorbit.app.data.healthconnect.HealthConnectManager
import com.healthorbit.app.data.local.SessionManager
import com.healthorbit.app.data.remote.ApiClient
import com.healthorbit.app.data.remote.HealthOrbitApiService
import com.healthorbit.app.data.repository.AuthRepository
import com.healthorbit.app.data.repository.WearableRepository

class AppContainer(val context: Context) {

    val sessionManager: SessionManager by lazy {
        SessionManager(context.applicationContext)
    }

    val healthConnectManager: HealthConnectManager by lazy {
        HealthConnectManager(context.applicationContext)
    }

    val apiService: HealthOrbitApiService by lazy {
        ApiClient.createService(context.applicationContext, sessionManager)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(apiService, sessionManager)
    }

    val wearableRepository: WearableRepository by lazy {
        WearableRepository(apiService, healthConnectManager, sessionManager)
    }
}
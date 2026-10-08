package com.healthorbit.app.data.repository

import com.healthorbit.app.data.local.SessionManager
import com.healthorbit.app.data.remote.GoogleMobileAuthRequest
import com.healthorbit.app.data.remote.HealthOrbitApiService
import com.healthorbit.app.data.remote.LoginRequest

class AuthRepository(
    private val apiService: HealthOrbitApiService,
    private val sessionManager: SessionManager
) {

    suspend fun login(request: LoginRequest): Result<String> {
        return try {
            val response = apiService.loginMobile(request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.token != null) {
                sessionManager.saveAuthToken(body.token)
                body.user?.let {
                    sessionManager.saveUser(it.id, it.email, it.fullName ?: "")
                }
                Result.success(body.token)
            } else {
                Result.failure(Exception(body?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<String> {
        return try {
            val response = apiService.googleMobileAuth(GoogleMobileAuthRequest(idToken))
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.token != null) {
                sessionManager.saveAuthToken(body.token)
                body.user?.let {
                    sessionManager.saveUser(it.id, it.email, it.fullName ?: "")
                }
                Result.success(body.token)
            } else {
                Result.failure(Exception(body?.message ?: "Google login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
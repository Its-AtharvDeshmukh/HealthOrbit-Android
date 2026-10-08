package com.healthorbit.app.data.remote

import android.content.Context
import com.healthorbit.app.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://healthorbit.onrender.com/"

    fun createService(context: Context, sessionManager: SessionManager): HealthOrbitApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val token = sessionManager.getToken()
                if (!token.isNullOrBlank()) {
                    val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                    requestBuilder.header("Authorization", authHeader)
                }
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HealthOrbitApiService::class.java)
    }
}
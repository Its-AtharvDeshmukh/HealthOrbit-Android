package com.healthorbit.app.data.remote

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class GoogleMobileAuthRequest(
    @SerializedName("idToken")
    val idToken: String
)

data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("fullName")
    val fullName: String?,
    @SerializedName("profileImage")
    val profileImage: String?
)

data class AuthResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("token")
    val token: String?,
    @SerializedName("user")
    val user: UserDto?
)
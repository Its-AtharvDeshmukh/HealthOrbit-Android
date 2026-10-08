package com.healthorbit.app.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.healthorbit.app.BuildConfig
import com.healthorbit.app.data.remote.LoginRequest
import com.healthorbit.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val errorMessage: String, val isMissingGoogleAccount: Boolean = false) : LoginUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        val rawPassword = password

        if (trimmedEmail.isBlank() || rawPassword.isBlank()) {
            _uiState.value = LoginUiState.Error("Please enter both email and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepository.login(
                LoginRequest(email = trimmedEmail, password = rawPassword)
            )
            result.fold(
                onSuccess = { _uiState.value = LoginUiState.Success },
                onFailure = { error ->
                    _uiState.value = LoginUiState.Error(error.message ?: "Authentication failed")
                }
            )
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {
                val credentialManager = CredentialManager.create(context)
                val serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID

                // Use exclusively GetGoogleIdOption to avoid option collision
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = context, request = request)
                handleGoogleCredential(result.credential)
            } catch (_: NoCredentialException) {
                _uiState.value = LoginUiState.Error(
                    errorMessage = "No Google account found on this device.",
                    isMissingGoogleAccount = true
                )
            } catch (_: GetCredentialCancellationException) {
                _uiState.value = LoginUiState.Idle
            } catch (e: GetCredentialException) {
                _uiState.value = LoginUiState.Error(e.message ?: "Google Sign-In canceled")
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Google Sign-In error")
            }
        }
    }

    fun openAddAccountSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallbackIntent = Intent(Settings.ACTION_SYNC_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
        }
    }

    private suspend fun handleGoogleCredential(credential: androidx.credentials.Credential) {
        var idToken: String? = null

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            idToken = googleIdTokenCredential.idToken
        }

        if (!idToken.isNullOrBlank()) {
            val authResult = authRepository.loginWithGoogle(idToken)
            authResult.fold(
                onSuccess = { _uiState.value = LoginUiState.Success },
                onFailure = { error ->
                    _uiState.value = LoginUiState.Error(error.message ?: "Google server login failed")
                }
            )
        } else {
            _uiState.value = LoginUiState.Error("Could not retrieve Google ID Token")
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                    return LoginViewModel(authRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
package com.healthorbit.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.healthorbit.app.data.local.SessionManager
import com.healthorbit.app.data.remote.WearableRecordDto
import com.healthorbit.app.data.repository.AuthRepository
import com.healthorbit.app.data.repository.WearableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SyncUiState {
    object Idle : SyncUiState()
    object Syncing : SyncUiState()
    object NeedsPermissions : SyncUiState()
    data class Success(val inserted: Int = 0, val skipped: Int = 0) : SyncUiState()
    data class Error(val message: String) : SyncUiState()
}

class DashboardViewModel(
    private val wearableRepository: WearableRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        fun provideFactory(
            wearableRepository: WearableRepository,
            authRepository: AuthRepository,
            sessionManager: SessionManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(wearableRepository, authRepository, sessionManager) as T
            }
        }
    }

    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()
    val syncUiState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    val wearableRecords: StateFlow<List<WearableRecordDto>> = wearableRepository.wearableRecords

    private val _lastSyncFormatted = MutableStateFlow("Never")
    val lastSyncFormatted: StateFlow<String> = _lastSyncFormatted.asStateFlow()

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    init {
        updateLastSyncLabel()
        checkPermissionsAndLoad()
    }

    fun isHealthConnectAvailable(): Boolean = wearableRepository.isHealthConnectAvailable()

    fun getRequiredPermissions(): Set<String> = wearableRepository.getRequiredPermissions()

    fun onPermissionsGranted() {
        _isPermissionGranted.value = true
        _syncState.value = SyncUiState.Idle
        checkPermissionsAndLoad()
    }

    fun onPermissionsDenied() {
        _isPermissionGranted.value = false
        _syncState.value = SyncUiState.NeedsPermissions
    }

    fun checkPermissionsAndLoad() {
        viewModelScope.launch {
            val granted = wearableRepository.hasPermissions()
            _isPermissionGranted.value = granted
            if (granted) {
                wearableRepository.fetchLocalSamples()
            } else {
                _syncState.value = SyncUiState.NeedsPermissions
            }
        }
    }

    fun onSyncClicked() {
        syncNow()
    }

    fun syncNow() {
        viewModelScope.launch {
            _syncState.value = SyncUiState.Syncing
            val result = wearableRepository.syncWearableData()
            result.onSuccess { count ->
                _syncState.value = SyncUiState.Success(inserted = count, skipped = 0)
                updateLastSyncLabel()
            }.onFailure { err ->
                _syncState.value = SyncUiState.Error(err.message ?: "Sync failed")
            }
        }
    }

    fun updateLastSyncLabel() {
        val lastSync = sessionManager.getLastSuccessfulWearableSyncAt()
        if (lastSync != null && lastSync > 0L) {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            _lastSyncFormatted.value = sdf.format(Date(lastSync))
        } else {
            _lastSyncFormatted.value = "Never"
        }
    }

    fun logout() {
        authRepository.logout()
    }

    // --- Direct Telemetry Bindings for DashboardScreen ---
    val steps: StateFlow<String> = wearableRecords.map { list ->
        val record = list.firstOrNull { it.metric == "steps" }
        if (record != null) "${record.value.toLong()}" else "--"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "--")

    val heartRate: StateFlow<String> = wearableRecords.map { list ->
        val record = list.firstOrNull { it.metric == "heart_rate" }
        if (record != null) "${record.value.toInt()}" else "--"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "--")

    val bloodPressure: StateFlow<String> = wearableRecords.map { list ->
        val sys = list.firstOrNull { it.metric == "blood_pressure_systolic" }?.value?.toInt()
        val dia = list.firstOrNull { it.metric == "blood_pressure_diastolic" }?.value?.toInt()
        if (sys != null && dia != null) "$sys/$dia" else "--"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "--")

    val calories: StateFlow<String> = wearableRecords.map { list ->
        val record = list.firstOrNull { it.metric == "calories" }
        if (record != null) "${record.value.toInt()}" else "--"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "--")

    val distanceKm: StateFlow<String> = wearableRecords.map { list ->
        val record = list.firstOrNull { it.metric == "distance" }
        if (record != null) "${record.value}" else "--"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "--")

    val spo2: StateFlow<String> = wearableRecords.map { list ->
        val record = list.firstOrNull { it.metric == "spo2" }
        if (record != null) "${record.value.toInt()}%" else "--"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "--")

    val sleepDuration: StateFlow<String> = wearableRecords.map { list ->
        val record = list.firstOrNull { it.metric == "sleep_duration" }
        if (record != null) "${record.value}h" else "--"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "--")

    val deepSleep: StateFlow<String> = wearableRecords.map { list ->
        val record = list.firstOrNull { it.metric == "deep_sleep" }
        if (record != null) "${record.value}h" else "--"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "--")

    val bodyTemp: StateFlow<String> = wearableRecords.map { list ->
        val record = list.firstOrNull { it.metric == "body_temperature" }
        if (record != null) "${record.value}°C" else "--"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "--")

    val stressHrv: StateFlow<String> = wearableRecords.map { list ->
        val record = list.firstOrNull { it.metric == "stress_hrv" }
        if (record != null) "${record.value.toInt()} ms" else "--"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "--")
}
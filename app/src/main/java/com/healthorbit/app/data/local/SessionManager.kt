package com.healthorbit.app.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "healthorbit_session"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"

        private const val KEY_LAST_SUCCESSFUL_SYNC_AT = "last_successful_wearable_sync_at"
        private const val KEY_LAST_SYNC_ATTEMPT_AT = "last_wearable_sync_attempt_at"
        private const val KEY_LAST_SYNC_STATUS = "last_wearable_sync_status"
    }

    fun saveAuthToken(token: String) {
        // commit() writes synchronously to flash storage to prevent token read races
        prefs.edit().putString(KEY_AUTH_TOKEN, token.trim()).commit()
    }

    fun getToken(): String? {
        val t = prefs.getString(KEY_AUTH_TOKEN, null)
        return if (!t.isNullOrBlank()) t.trim() else null
    }

    fun isLoggedIn(): Boolean {
        val t = getToken()
        return !t.isNullOrBlank()
    }

    fun saveUser(id: String, email: String, name: String) {
        prefs.edit()
            .putString(KEY_USER_ID, id)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .commit()
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun clearSession() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .commit()
        clearWearableSyncState()
    }

    fun getLastSuccessfulWearableSyncAt(): Long? {
        val timestamp = prefs.getLong(KEY_LAST_SUCCESSFUL_SYNC_AT, -1L)
        return if (timestamp > 0L) timestamp else null
    }

    fun setLastSuccessfulWearableSyncAt(epochMillis: Long) {
        prefs.edit()
            .putLong(KEY_LAST_SUCCESSFUL_SYNC_AT, epochMillis)
            .putString(KEY_LAST_SYNC_STATUS, "SUCCESS")
            .putLong(KEY_LAST_SYNC_ATTEMPT_AT, System.currentTimeMillis())
            .apply()
    }

    fun setLastWearableSyncAttempt(status: String) {
        prefs.edit()
            .putLong(KEY_LAST_SYNC_ATTEMPT_AT, System.currentTimeMillis())
            .putString(KEY_LAST_SYNC_STATUS, status)
            .apply()
    }

    fun getLastWearableSyncAttemptAt(): Long? {
        val timestamp = prefs.getLong(KEY_LAST_SYNC_ATTEMPT_AT, -1L)
        return if (timestamp > 0L) timestamp else null
    }

    fun getLastWearableSyncStatus(): String {
        return prefs.getString(KEY_LAST_SYNC_STATUS, "NEVER_SYNCED") ?: "NEVER_SYNCED"
    }

    fun clearWearableSyncState() {
        prefs.edit()
            .remove(KEY_LAST_SUCCESSFUL_SYNC_AT)
            .remove(KEY_LAST_SYNC_ATTEMPT_AT)
            .remove(KEY_LAST_SYNC_STATUS)
            .apply()
    }
}
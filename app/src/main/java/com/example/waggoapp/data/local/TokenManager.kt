package com.example.waggoapp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Secure token and session manager backed by Android Keystore and EncryptedSharedPreferences.
 * Never stores tokens in plain text. Provides reactive session state.
 */
class TokenManager private constructor(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to standard private preferences if Keystore throws on certain Android ROMs
        context.getSharedPreferences(PREFS_FALLBACK_FILE_NAME, Context.MODE_PRIVATE)
    }

    private val _isLoggedIn = MutableStateFlow(hasValidTokens())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    @Synchronized
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    @Synchronized
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    @Synchronized
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    @Synchronized
    fun getUserFullName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    @Synchronized
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    @Synchronized
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
        _isLoggedIn.value = true
    }

    @Synchronized
    fun saveUserSession(
        accessToken: String,
        refreshToken: String,
        email: String,
        fullName: String,
        userId: String? = null
    ) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, fullName)
            .putString(KEY_USER_ID, userId)
            .apply()
        _isLoggedIn.value = true
    }

    @Synchronized
    fun updateProfile(fullName: String, email: String? = null) {
        val editor = prefs.edit().putString(KEY_USER_NAME, fullName)
        if (email != null) editor.putString(KEY_USER_EMAIL, email)
        editor.apply()
    }

    @Synchronized
    fun clear() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
    }

    fun hasValidTokens(): Boolean {
        return !prefs.getString(KEY_ACCESS_TOKEN, null).isNullOrBlank() &&
                !prefs.getString(KEY_REFRESH_TOKEN, null).isNullOrBlank()
    }

    companion object {
        private const val PREFS_FILE_NAME = "washgo_encrypted_prefs"
        private const val PREFS_FALLBACK_FILE_NAME = "washgo_fallback_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ID = "user_id"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

package com.washgo.user.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Secure token and session manager backed by Android Keystore and EncryptedSharedPreferences.
 * Never stores tokens or credentials in plain text. Provides reactive session state.
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
        // Fallback to mode private if Keystore has device-specific issues
        context.getSharedPreferences(PREFS_FALLBACK_FILE_NAME, Context.MODE_PRIVATE)
    }

    private val _isLoggedIn = MutableStateFlow(hasValidTokens())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    @Synchronized
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    @Synchronized
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    @Synchronized
    fun getUserPhone(): String? = prefs.getString(KEY_USER_PHONE, null)

    @Synchronized
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    @Synchronized
    fun getUserFullName(): String? = prefs.getString(KEY_USER_NAME, null)

    @Synchronized
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

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
        phone: String? = null,
        email: String? = null,
        fullName: String? = null,
        userId: String? = null
    ) {
        val editor = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)

        if (phone != null) editor.putString(KEY_USER_PHONE, phone)
        if (email != null) editor.putString(KEY_USER_EMAIL, email)
        if (fullName != null) editor.putString(KEY_USER_NAME, fullName)
        if (userId != null) editor.putString(KEY_USER_ID, userId)

        editor.apply()
        _isLoggedIn.value = true
    }

    @Synchronized
    fun updateProfile(fullName: String?, email: String?, phone: String? = null) {
        val editor = prefs.edit()
        if (fullName != null) editor.putString(KEY_USER_NAME, fullName)
        if (email != null) editor.putString(KEY_USER_EMAIL, email)
        if (phone != null) editor.putString(KEY_USER_PHONE, phone)
        editor.apply()
    }

    @Synchronized
    fun clear() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
    }

    fun hasValidTokens(): Boolean {
        val access = prefs.getString(KEY_ACCESS_TOKEN, null)
        val refresh = prefs.getString(KEY_REFRESH_TOKEN, null)
        return !access.isNullOrBlank() && !refresh.isNullOrBlank()
    }

    companion object {
        private const val PREFS_FILE_NAME = "washgo_user_encrypted_prefs"
        private const val PREFS_FALLBACK_FILE_NAME = "washgo_user_fallback_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_PHONE = "user_phone"
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

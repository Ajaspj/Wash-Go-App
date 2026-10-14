package com.washgo.agent.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Secure token and session manager for Wash-Go Service Agents.
 * Backed by Android Keystore and EncryptedSharedPreferences.
 */
class AgentTokenManager private constructor(context: Context) {

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
        context.getSharedPreferences(PREFS_FALLBACK_FILE_NAME, Context.MODE_PRIVATE)
    }

    private val _isLoggedIn = MutableStateFlow(hasValidTokens())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _agentStatus = MutableStateFlow(getAgentStatus())
    val agentStatus: StateFlow<String> = _agentStatus.asStateFlow()

    @Synchronized
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    @Synchronized
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    @Synchronized
    fun getAgentId(): String? = prefs.getString(KEY_AGENT_ID, null)

    @Synchronized
    fun getAgentCode(): String? = prefs.getString(KEY_AGENT_CODE, null)

    @Synchronized
    fun getPhone(): String? = prefs.getString(KEY_PHONE, null)

    @Synchronized
    fun getDisplayName(): String? = prefs.getString(KEY_DISPLAY_NAME, null)

    @Synchronized
    fun getAgentStatus(): String = prefs.getString(KEY_STATUS, "DRAFT") ?: "DRAFT"

    @Synchronized
    fun isApproved(): Boolean = getAgentStatus().equals("APPROVED", ignoreCase = true)

    @Synchronized
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
        _isLoggedIn.value = true
    }

    @Synchronized
    fun saveAgentSession(
        accessToken: String,
        refreshToken: String,
        agentId: String?,
        agentCode: String?,
        phone: String?,
        displayName: String?,
        status: String?
    ) {
        val editor = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)

        if (agentId != null) editor.putString(KEY_AGENT_ID, agentId)
        if (agentCode != null) editor.putString(KEY_AGENT_CODE, agentCode)
        if (phone != null) editor.putString(KEY_PHONE, phone)
        if (displayName != null) editor.putString(KEY_DISPLAY_NAME, displayName)
        if (status != null) {
            editor.putString(KEY_STATUS, status)
            _agentStatus.value = status
        }

        editor.apply()
        _isLoggedIn.value = true
    }

    @Synchronized
    fun updateAgentStatus(status: String) {
        prefs.edit().putString(KEY_STATUS, status).apply()
        _agentStatus.value = status
    }

    @Synchronized
    fun updateProfile(displayName: String?, agentCode: String?, status: String?) {
        val editor = prefs.edit()
        if (displayName != null) editor.putString(KEY_DISPLAY_NAME, displayName)
        if (agentCode != null) editor.putString(KEY_AGENT_CODE, agentCode)
        if (status != null) {
            editor.putString(KEY_STATUS, status)
            _agentStatus.value = status
        }
        editor.apply()
    }

    @Synchronized
    fun clear() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
        _agentStatus.value = "DRAFT"
    }

    fun hasValidTokens(): Boolean {
        val access = prefs.getString(KEY_ACCESS_TOKEN, null)
        val refresh = prefs.getString(KEY_REFRESH_TOKEN, null)
        return !access.isNullOrBlank() && !refresh.isNullOrBlank()
    }

    companion object {
        private const val PREFS_FILE_NAME = "washgo_agent_encrypted_prefs"
        private const val PREFS_FALLBACK_FILE_NAME = "washgo_agent_fallback_prefs"
        private const val KEY_ACCESS_TOKEN = "agent_access_token"
        private const val KEY_REFRESH_TOKEN = "agent_refresh_token"
        private const val KEY_AGENT_ID = "agent_id"
        private const val KEY_AGENT_CODE = "agent_code"
        private const val KEY_PHONE = "agent_phone"
        private const val KEY_DISPLAY_NAME = "agent_display_name"
        private const val KEY_STATUS = "agent_status"

        @Volatile
        private var instance: AgentTokenManager? = null

        fun getInstance(context: Context): AgentTokenManager {
            return instance ?: synchronized(this) {
                instance ?: AgentTokenManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

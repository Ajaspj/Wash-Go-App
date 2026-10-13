package com.example.waggoapp.data.network

import com.example.waggoapp.BuildConfig

/**
 * Manages dynamic base URL for physical device testing via `adb reverse` or local network IP.
 */
object ApiConfig {

    // Default to 127.0.0.1:8080 for physical device via `adb reverse tcp:8080 tcp:8080`
    private const val DEFAULT_URL = "http://127.0.0.1:8080/"

    @Volatile
    private var customBaseUrl: String? = null

    fun getBaseUrl(): String {
        return customBaseUrl ?: run {
            val buildConfigUrl = BuildConfig.BASE_URL
            if (buildConfigUrl.isNotBlank() && buildConfigUrl != "http://10.0.2.2:8080/") {
                buildConfigUrl
            } else {
                DEFAULT_URL
            }
        }
    }

    fun setBaseUrl(url: String) {
        val sanitized = if (url.endsWith("/")) url else "$url/"
        customBaseUrl = sanitized
    }
}

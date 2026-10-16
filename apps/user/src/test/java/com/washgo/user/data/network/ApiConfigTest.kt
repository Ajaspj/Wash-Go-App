package com.washgo.user.data.network

import com.washgo.user.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiConfigTest {

    @Test
    fun baseUrl_configuredCorrectlyPerBuildType() {
        assertTrue("Base URL must end with trailing slash for Retrofit", BuildConfig.BASE_URL.endsWith("/"))

        if (BuildConfig.BUILD_TYPE == "debug") {
            assertEquals(
                "Debug build must target staging backend",
                "https://wash-go-api-hsvt.onrender.com/",
                BuildConfig.BASE_URL
            )
            assertFalse(
                "Debug build must never target production backend",
                BuildConfig.BASE_URL == "https://wash-go-api.onrender.com/"
            )
        } else if (BuildConfig.BUILD_TYPE == "release") {
            assertEquals(
                "Release build must target production backend",
                "https://wash-go-api.onrender.com/",
                BuildConfig.BASE_URL
            )
        }
    }

    @Test
    fun derivedWebSocketUrl_matchesConfiguredBaseUrl() {
        val wsBase = BuildConfig.BASE_URL.replace("http://", "ws://").replace("https://", "wss://").trimEnd('/') + "/ws"
        if (BuildConfig.BUILD_TYPE == "debug") {
            assertEquals("wss://wash-go-api-hsvt.onrender.com/ws", wsBase)
        } else if (BuildConfig.BUILD_TYPE == "release") {
            assertEquals("wss://wash-go-api.onrender.com/ws", wsBase)
        }
    }

    @Test
    fun derivedTokenRefreshUrl_matchesConfiguredBaseUrl() {
        val refreshUrl = BuildConfig.BASE_URL.removeSuffix("/") + "/api/v1/auth/refresh"
        if (BuildConfig.BUILD_TYPE == "debug") {
            assertEquals("https://wash-go-api-hsvt.onrender.com/api/v1/auth/refresh", refreshUrl)
        } else if (BuildConfig.BUILD_TYPE == "release") {
            assertEquals("https://wash-go-api.onrender.com/api/v1/auth/refresh", refreshUrl)
        }
    }
}

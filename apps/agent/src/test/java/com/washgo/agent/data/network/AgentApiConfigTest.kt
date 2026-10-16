package com.washgo.agent.data.network

import com.washgo.agent.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentApiConfigTest {

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
        val baseWs = BuildConfig.BASE_URL
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/')

        val wsUrl = "$baseWs/ws/booking?agentId=agent-123&role=AGENT"

        if (BuildConfig.BUILD_TYPE == "debug") {
            assertEquals("wss://wash-go-api-hsvt.onrender.com", baseWs)
            assertEquals("wss://wash-go-api-hsvt.onrender.com/ws/booking?agentId=agent-123&role=AGENT", wsUrl)
        } else if (BuildConfig.BUILD_TYPE == "release") {
            assertEquals("wss://wash-go-api.onrender.com", baseWs)
            assertEquals("wss://wash-go-api.onrender.com/ws/booking?agentId=agent-123&role=AGENT", wsUrl)
        }
    }

    @Test
    fun derivedDispatchWebSocketUrl_matchesConfiguredBaseUrl() {
        val wsUrl = BuildConfig.BASE_URL.replace("http://", "ws://").replace("https://", "wss://").trimEnd('/') + "/ws"
        if (BuildConfig.BUILD_TYPE == "debug") {
            assertEquals("wss://wash-go-api-hsvt.onrender.com/ws", wsUrl)
        } else if (BuildConfig.BUILD_TYPE == "release") {
            assertEquals("wss://wash-go-api.onrender.com/ws", wsUrl)
        }
    }

    @Test
    fun derivedTokenRefreshUrl_matchesConfiguredBaseUrl() {
        val refreshUrl = BuildConfig.BASE_URL.trimEnd('/') + "/api/v1/auth/refresh"
        if (BuildConfig.BUILD_TYPE == "debug") {
            assertEquals("https://wash-go-api-hsvt.onrender.com/api/v1/auth/refresh", refreshUrl)
        } else if (BuildConfig.BUILD_TYPE == "release") {
            assertEquals("https://wash-go-api.onrender.com/api/v1/auth/refresh", refreshUrl)
        }
    }
}

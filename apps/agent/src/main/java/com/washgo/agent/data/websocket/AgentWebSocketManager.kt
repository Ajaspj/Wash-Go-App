package com.washgo.agent.data.websocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.washgo.agent.BuildConfig
import com.washgo.agent.data.model.AgentAcceptanceMessage
import com.washgo.agent.data.model.AgentJobNotificationMessage
import com.washgo.agent.data.model.AgentLocationUpdate
import com.washgo.agent.data.model.BookingStatusUpdateMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

object AgentWebSocketManager {

    private const val TAG = "AgentWebSocketManager"
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var activeWebSocket: WebSocket? = null
    private var currentAgentId: String = "00000000-0000-0000-0000-000000000001"

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _incomingJobFlow = MutableSharedFlow<AgentJobNotificationMessage>(extraBufferCapacity = 10)
    val incomingJobFlow: SharedFlow<AgentJobNotificationMessage> = _incomingJobFlow.asSharedFlow()

    private val _statusUpdateFlow = MutableSharedFlow<BookingStatusUpdateMessage>(extraBufferCapacity = 10)
    val statusUpdateFlow: SharedFlow<BookingStatusUpdateMessage> = _statusUpdateFlow.asSharedFlow()

    fun connect(agentId: String = "00000000-0000-0000-0000-000000000001", token: String? = null) {
        if (activeWebSocket != null) return
        currentAgentId = agentId

        val baseWs = BuildConfig.BASE_URL
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/')

        val tokenQuery = if (!token.isNullOrBlank()) "&token=$token" else ""
        val wsUrl = "$baseWs/ws/booking?agentId=$agentId&role=AGENT$tokenQuery"
        Log.i(TAG, "Connecting to WebSocket: $wsUrl")

        val requestBuilder = Request.Builder().url(wsUrl)
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        val request = requestBuilder.build()

        activeWebSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(TAG, "WebSocket connected successfully for agent: $agentId")
                _isConnected.value = true

                // Send registration handshake
                val registerPayload = JsonObject().apply {
                    addProperty("action", "REGISTER_AGENT")
                    addProperty("agentId", agentId)
                    addProperty("role", "AGENT")
                }
                webSocket.send(registerPayload.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "WebSocket message received: $text")
                handleIncomingMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}", t)
                _isConnected.value = false
                activeWebSocket = null
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "WebSocket closed ($code): $reason")
                _isConnected.value = false
                activeWebSocket = null
            }
        })
    }

    fun disconnect() {
        Log.i(TAG, "Disconnecting WebSocket")
        activeWebSocket?.close(1000, "Agent went offline")
        activeWebSocket = null
        _isConnected.value = false
    }

    fun sendAcceptance(acceptance: AgentAcceptanceMessage) {
        val json = gson.toJson(acceptance)
        Log.i(TAG, "Sending AgentAcceptanceMessage: $json")
        val sent = activeWebSocket?.send(json) ?: false
        if (!sent) {
            Log.w(TAG, "Failed sending acceptance over WebSocket, socket not active")
        }
    }

    fun sendLocationUpdate(location: AgentLocationUpdate) {
        val json = gson.toJson(location)
        activeWebSocket?.send(json)
    }

    private fun handleIncomingMessage(text: String) {
        try {
            val root = JsonParser.parseString(text).asJsonObject
            val type = root.get("type")?.asString ?: ""

            when {
                // Incoming Job Notification
                type.equals("JOB_NOTIFICATION", ignoreCase = true) || root.has("earningsEstimate") -> {
                    val data = if (root.has("data") && root.get("data").isJsonObject) {
                        root.getAsJsonObject("data")
                    } else {
                        root
                    }
                    val notification = gson.fromJson(data, AgentJobNotificationMessage::class.java)
                    Log.i(TAG, "Dispatched incoming job offer: ${notification.bookingId}")
                    scope.launch { _incomingJobFlow.emit(notification) }
                }

                // Booking Status Update
                type.equals("STATUS_UPDATE", ignoreCase = true) || root.has("status") -> {
                    val data = if (root.has("data") && root.get("data").isJsonObject) {
                        root.getAsJsonObject("data")
                    } else {
                        root
                    }
                    val update = gson.fromJson(data, BookingStatusUpdateMessage::class.java)
                    Log.i(TAG, "Received booking status update: ${update.status}")
                    scope.launch { _statusUpdateFlow.emit(update) }
                }

                type.equals("PONG", ignoreCase = true) -> {
                    Log.d(TAG, "Received PONG heartbeat")
                }
            }

        } catch (e: Exception) {
            Log.w(TAG, "Failed parsing WebSocket message: ${e.message}")
        }
    }

    // Helper for manual preview / testing when server is offline
    fun simulateIncomingOffer(offer: AgentJobNotificationMessage) {
        scope.launch { _incomingJobFlow.emit(offer) }
    }
}

package com.washgo.user.data.network

import com.washgo.user.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

object UserWebSocketClient {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeWebSocket: WebSocket? = null

    private val _matchEvents = MutableSharedFlow<BookingMatchPush>(extraBufferCapacity = 10)
    val matchEvents: SharedFlow<BookingMatchPush> = _matchEvents.asSharedFlow()

    fun connect(bookingId: String? = null) {
        if (activeWebSocket != null) return

        val wsUrl = BuildConfig.BASE_URL.replace("http://", "ws://").replace("https://", "wss://") + "ws"
        val request = Request.Builder().url(wsUrl).build()

        activeWebSocket = ApiClient.okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Send STOMP CONNECT frame
                val connectFrame = "CONNECT\naccept-version:1.2,1.1,1.0\nheart-beat:10000,10000\n\n\u0000"
                webSocket.send(connectFrame)

                // Subscribe to user bookings and specific booking topic
                val subUser = "SUBSCRIBE\nid:sub-user\ndestination:/topic/user-bookings\n\n\u0000"
                webSocket.send(subUser)

                if (bookingId != null) {
                    val subBooking = "SUBSCRIBE\nid:sub-bk\ndestination:/topic/booking/$bookingId/tracking\n\n\u0000"
                    webSocket.send(subBooking)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val bodyIndex = text.indexOf("\n\n")
                    if (bodyIndex != -1) {
                        val body = text.substring(bodyIndex + 2).replace("\u0000", "").trim()
                        if (body.startsWith("{")) {
                            val json = JSONObject(body)
                            val match = BookingMatchPush(
                                bookingId = json.optString("bookingId", ""),
                                status = json.optString("status", "ACCEPTED"),
                                agentId = json.optString("agentId", ""),
                                agentName = json.optString("agentName", "Ramesh Kumar"),
                                agentPhone = json.optString("agentPhone", "+91 98765 43210"),
                                agentRating = json.optDouble("agentRating", 4.9),
                                agentLatitude = json.optDouble("agentLatitude", 12.9734),
                                agentLongitude = json.optDouble("agentLongitude", 77.5962),
                                etaMinutes = json.optInt("etaMinutes", 3),
                                startOtp = json.optString("startOtp", "4821")
                            )
                            scope.launch { _matchEvents.emit(match) }
                        }
                    }
                } catch (_: Exception) {
                    // Ignore framing anomalies
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                activeWebSocket = null
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                activeWebSocket = null
            }
        })
    }

    fun disconnect() {
        activeWebSocket?.close(1000, "Normal closure")
        activeWebSocket = null
    }

    fun simulateMockMatch(match: BookingMatchPush) {
        scope.launch { _matchEvents.emit(match) }
    }
}

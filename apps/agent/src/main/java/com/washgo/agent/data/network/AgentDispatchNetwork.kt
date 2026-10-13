package com.washgo.agent.data.network

import com.washgo.agent.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class AgentApiResponse<T>(
    val data: T?,
    val message: String? = null,
    val code: String? = null
)

data class IncomingJobOffer(
    val offerId: String,
    val bookingId: String,
    val serviceName: String,
    val vehicleCategory: String,
    val customerAddress: String,
    val customerLatitude: Double,
    val customerLongitude: Double,
    val distanceMeters: Int,
    val estimatedDurationSeconds: Int,
    val agentEarnings: Double,
    val durationSeconds: Int = 20
)

data class AcceptBookingPayload(
    val bookingId: String,
    val agentId: String
)

data class JobAcceptanceConfirmation(
    val bookingId: String,
    val status: String,
    val agentId: String,
    val agentName: String,
    val startOtp: String
)

interface AgentDispatchApi {
    @POST("api/bookings/{bookingId}/accept")
    suspend fun acceptBooking(
        @Path("bookingId") bookingId: String,
        @Body payload: AcceptBookingPayload
    ): AgentApiResponse<JobAcceptanceConfirmation>
}

object AgentNetworkClient {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val api: AgentDispatchApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AgentDispatchApi::class.java)
    }

    private val _incomingOffers = MutableSharedFlow<IncomingJobOffer>(extraBufferCapacity = 10)
    val incomingOffers: SharedFlow<IncomingJobOffer> = _incomingOffers.asSharedFlow()

    private var activeWebSocket: WebSocket? = null

    fun connectWebSocket(agentId: String = "00000000-0000-0000-0000-000000000001") {
        if (activeWebSocket != null) return

        val wsUrl = BuildConfig.BASE_URL.replace("http://", "ws://").replace("https://", "wss://") + "ws"
        val request = Request.Builder().url(wsUrl).build()

        activeWebSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Send STOMP CONNECT frame
                val connectFrame = "CONNECT\naccept-version:1.2,1.1,1.0\nheart-beat:10000,10000\n\n\u0000"
                webSocket.send(connectFrame)

                // Subscribe to agent jobs topic
                val subFrame = "SUBSCRIBE\nid:sub-0\ndestination:/topic/agent-jobs\n\n\u0000"
                webSocket.send(subFrame)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    // Extract STOMP JSON body (after double newline, ending before NULL char)
                    val bodyIndex = text.indexOf("\n\n")
                    if (bodyIndex != -1) {
                        val body = text.substring(bodyIndex + 2).replace("\u0000", "").trim()
                        if (body.startsWith("{")) {
                            val json = JSONObject(body)
                            val offer = IncomingJobOffer(
                                offerId = json.optString("offerId", ""),
                                bookingId = json.optString("bookingId", ""),
                                serviceName = json.optString("serviceName", "Sedan Foam & Detail Wash"),
                                vehicleCategory = json.optString("vehicleCategory", "SEDAN"),
                                customerAddress = json.optString("customerAddress", "12th Main Road, Indiranagar"),
                                customerLatitude = json.optDouble("customerLatitude", 12.9716),
                                customerLongitude = json.optDouble("customerLongitude", 77.5946),
                                distanceMeters = json.optInt("distanceMeters", 850),
                                estimatedDurationSeconds = json.optInt("estimatedDurationSeconds", 180),
                                agentEarnings = json.optDouble("agentEarnings", 245.0),
                                durationSeconds = json.optInt("durationSeconds", 20)
                            )
                            scope.launch { _incomingOffers.emit(offer) }
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

    // Fallback broadcast simulation for local offline testing when backend is not actively running
    fun simulateMockBroadcastOffer(offer: IncomingJobOffer) {
        scope.launch { _incomingOffers.emit(offer) }
    }
}

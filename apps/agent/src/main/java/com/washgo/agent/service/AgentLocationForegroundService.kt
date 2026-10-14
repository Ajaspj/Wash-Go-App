package com.washgo.agent.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.washgo.agent.AgentMainActivity
import com.washgo.agent.data.model.AgentLocationUpdate
import com.washgo.agent.data.websocket.AgentWebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AgentLocationForegroundService : Service {

    constructor() : super()

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var trackingJob: Job? = null

    private var currentLat = 12.9716
    private var currentLng = 77.5946
    private var currentHeading = 45f
    private var agentId = "00000000-0000-0000-0000-000000000001"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        if (action == ACTION_STOP) {
            stopTracking()
            stopSelf()
            return START_NOT_STICKY
        }

        agentId = intent?.getStringExtra(EXTRA_AGENT_ID) ?: agentId
        startForegroundServiceNotification()
        startLocationSimulationLoop()

        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val notificationIntent = Intent(this, AgentMainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wash-Go Partner Online")
            .setContentText("Broadcasting live GPS coordinates for auto-dispatch...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startLocationSimulationLoop() {
        if (trackingJob?.isActive == true) return

        trackingJob = serviceScope.launch {
            Log.i(TAG, "Starting GPS simulation loop for agent: $agentId")
            var step = 0
            while (isActive) {
                // Mock micro-movement (cruising at ~20-25 km/h around city streets)
                val angle = Math.toRadians((step * 15.0) % 360.0)
                val deltaLat = 0.00015 * Math.sin(angle)
                val deltaLng = 0.00018 * Math.cos(angle)

                currentLat += deltaLat
                currentLng += deltaLng
                currentHeading = ((step * 15f) % 360f)

                val locationUpdate = AgentLocationUpdate(
                    agentId = agentId,
                    latitude = currentLat,
                    longitude = currentLng,
                    speedKmph = 24.5,
                    heading = currentHeading,
                    timestamp = System.currentTimeMillis()
                )

                _liveLocationState.value = locationUpdate

                // Transmit live coordinates to backend over WebSocket
                AgentWebSocketManager.sendLocationUpdate(locationUpdate)
                Log.d(TAG, "Transmitted location: lat=$currentLat, lng=$currentLng")

                // Synchronize coordinates to authenticated backend endpoint periodically
                if (step % 3 == 0) {
                    try {
                        com.washgo.agent.data.repository.AgentRepository.getInstance(applicationContext)
                            .updateLocation(currentLat, currentLng, currentHeading.toDouble())
                    } catch (_: Exception) {
                        // Tolerant to momentary network hiccups
                    }
                }

                step++
                delay(LOCATION_INTERVAL_MS)
            }
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        _liveLocationState.value = null
        Log.i(TAG, "Stopped GPS simulation loop")
    }

    override fun onDestroy() {
        stopTracking()
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wash-Go Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used to maintain foreground location broadcast to customers"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "AgentLocationService"
        private const val CHANNEL_ID = "washgo_agent_location_channel"
        private const val NOTIFICATION_ID = 4001
        private const val LOCATION_INTERVAL_MS = 4000L

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_AGENT_ID = "EXTRA_AGENT_ID"

        private val _liveLocationState = MutableStateFlow<AgentLocationUpdate?>(null)
        val liveLocationState: StateFlow<AgentLocationUpdate?> = _liveLocationState.asStateFlow()

        fun start(context: Context, agentId: String = "00000000-0000-0000-0000-000000000001") {
            val intent = Intent(context, AgentLocationForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_AGENT_ID, agentId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AgentLocationForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}

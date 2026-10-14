package com.washgo.agent.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.washgo.agent.BuildConfig
import com.washgo.agent.data.local.AgentTokenManager
import com.washgo.agent.data.model.AgentAcceptanceMessage
import com.washgo.agent.data.model.AgentJobNotificationMessage
import com.washgo.agent.data.model.AgentLocationUpdate
import com.washgo.agent.data.model.AgentProfileDto
import com.washgo.agent.data.model.AgentStatsDto
import com.washgo.agent.data.model.BookingStatusUpdateMessage
import com.washgo.agent.data.repository.AgentRepository
import com.washgo.agent.data.websocket.AgentWebSocketManager
import com.washgo.agent.service.AgentLocationForegroundService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class AgentDashboardViewModel(
    private val repository: AgentRepository? = null,
    private val tokenManager: AgentTokenManager? = null
) : ViewModel() {

    private val _agentProfile = MutableStateFlow<AgentProfileDto?>(null)
    val agentProfile: StateFlow<AgentProfileDto?> = _agentProfile.asStateFlow()

    private val _stats = MutableStateFlow(AgentStatsDto())
    val stats: StateFlow<AgentStatsDto> = _stats.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _incomingJob = MutableStateFlow<AgentJobNotificationMessage?>(null)
    val incomingJob: StateFlow<AgentJobNotificationMessage?> = _incomingJob.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(15)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _activeJob = MutableStateFlow<AgentJobNotificationMessage?>(null)
    val activeJob: StateFlow<AgentJobNotificationMessage?> = _activeJob.asStateFlow()

    private val _latestStatusUpdate = MutableStateFlow<BookingStatusUpdateMessage?>(null)
    val latestStatusUpdate: StateFlow<BookingStatusUpdateMessage?> = _latestStatusUpdate.asStateFlow()

    val currentLocation: StateFlow<AgentLocationUpdate?> = AgentLocationForegroundService.liveLocationState
    val isWebSocketConnected: StateFlow<Boolean> = AgentWebSocketManager.isConnected

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    private var countdownJob: Job? = null

    init {
        loadDashboardData()

        // Collect incoming job notifications pushed over WebSocket
        viewModelScope.launch {
            AgentWebSocketManager.incomingJobFlow.collect { notification ->
                if (_activeJob.value == null) {
                    onJobNotificationReceived(notification)
                }
            }
        }

        // Collect booking status updates (simulation progression)
        viewModelScope.launch {
            AgentWebSocketManager.statusUpdateFlow.collect { update ->
                _latestStatusUpdate.value = update
            }
        }
    }

    fun loadDashboardData() {
        val repo = repository ?: return
        viewModelScope.launch {
            _isLoading.value = true
            // Load Profile
            repo.getProfile().onSuccess { profile ->
                _agentProfile.value = profile
                _isOnline.value = profile.isOnline
            }

            // Load Stats
            repo.getStats().onSuccess { statsDto ->
                _stats.value = statsDto
            }
            _isLoading.value = false
        }
    }

    fun toggleOnline(context: Context, online: Boolean) {
        val tm = tokenManager
        val repo = repository
        val status = tm?.getAgentStatus() ?: if (online) "APPROVED" else "OFFLINE"
        if (online && !status.equals("APPROVED", ignoreCase = true)) {
            _snackbarMessages.tryEmit("Cannot go online. Partner status is $status (APPROVED required).")
            _isOnline.value = false
            return
        }

        if (repo == null) {
            _isOnline.value = online
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = repo.toggleOnline(online)
            _isLoading.value = false

            result.fold(
                onSuccess = { updatedProfile ->
                    _isOnline.value = updatedProfile.isOnline
                    _agentProfile.value = updatedProfile

                    if (updatedProfile.isOnline) {
                        val agentId = tm?.getAgentId() ?: updatedProfile.id
                        val token = tm?.getAccessToken() ?: ""
                        AgentWebSocketManager.connect(agentId, token)
                        AgentLocationForegroundService.start(context, agentId)
                        _snackbarMessages.tryEmit("You are ONLINE. Broadcasting GPS & ready for wash requests.")
                    } else {
                        AgentWebSocketManager.disconnect()
                        AgentLocationForegroundService.stop(context)
                        declineJob()
                        _activeJob.value = null
                        _snackbarMessages.tryEmit("You are OFFLINE. Dispatch tracking paused.")
                    }
                },
                onFailure = { error ->
                    _isOnline.value = false
                    _snackbarMessages.tryEmit(error.message ?: "Failed to update online state.")
                }
            )
        }
    }

    private fun onJobNotificationReceived(notification: AgentJobNotificationMessage) {
        _incomingJob.value = notification
        _countdownSeconds.value = notification.timeoutSeconds ?: 15

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_countdownSeconds.value > 0 && _incomingJob.value != null) {
                delay(1000)
                _countdownSeconds.value -= 1
            }
            if (_countdownSeconds.value == 0 && _incomingJob.value != null) {
                declineJob()
                _snackbarMessages.emit("Offer timed out. You missed a booking.")
            }
        }
    }

    fun acceptJob() {
        val job = _incomingJob.value ?: return
        countdownJob?.cancel()

        val loc = currentLocation.value
        val lat = loc?.latitude ?: 12.9716
        val lng = loc?.longitude ?: 77.5946
        val agentId = tokenManager?.getAgentId() ?: "00000000-0000-0000-0000-000000000001"

        viewModelScope.launch {
            // Call HTTP accept endpoint
            repository?.acceptBooking(job.bookingId)

            val acceptance = AgentAcceptanceMessage(
                action = "AGENT_ACCEPT",
                bookingId = job.bookingId,
                agentId = agentId,
                agentLatitude = lat,
                agentLongitude = lng,
                acceptedAt = Instant.now().toString()
            )

            // Dispatch acceptance over WebSocket
            AgentWebSocketManager.sendAcceptance(acceptance)

            _activeJob.value = job
            _incomingJob.value = null
            _snackbarMessages.tryEmit("Booking accepted! Navigating to customer location.")
        }
    }

    fun declineJob() {
        val job = _incomingJob.value
        countdownJob?.cancel()
        _incomingJob.value = null

        if (job != null) {
            viewModelScope.launch {
                repository?.rejectBooking(job.bookingId, "DECLINED_BY_AGENT")
            }
        }
    }

    fun finishActiveJob() {
        _activeJob.value = null
        _latestStatusUpdate.value = null
        loadDashboardData()
        _snackbarMessages.tryEmit("Job finished. Partner payout credited.")
    }

    fun simulateIncomingOffer() {
        if (!BuildConfig.DEBUG) return
        val mockOffer = AgentJobNotificationMessage(
            bookingId = "bk-" + System.currentTimeMillis().toString().takeLast(5),
            userId = "usr-404",
            customerName = "Ananya Sharma",
            customerLatitude = 12.9716,
            customerLongitude = 77.5946,
            customerAddress = "12th Main Road, Indiranagar, Bengaluru",
            vehicleType = "Sedan",
            earningsEstimate = 320.0,
            timeoutSeconds = 15,
            requestedAt = Instant.now().toString()
        )
        AgentWebSocketManager.simulateIncomingOffer(mockOffer)
    }

    fun setAuditOnline(online: Boolean) {
        _isOnline.value = online
    }

    fun setAuditActiveJob(active: Boolean) {
        if (active) {
            _activeJob.value = AgentJobNotificationMessage(
                bookingId = "bk-88219",
                userId = "usr-404",
                customerName = "Ananya Sharma",
                customerLatitude = 12.9716,
                customerLongitude = 77.5946,
                customerAddress = "12th Main Road, Indiranagar, Bengaluru",
                vehicleType = "Sedan",
                earningsEstimate = 320.0,
                timeoutSeconds = 15,
                requestedAt = Instant.now().toString()
            )
        } else {
            _activeJob.value = null
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            if (_isOnline.value) {
                toggleOnline(context, false)
            }
            repository?.logout()
            _snackbarMessages.tryEmit("Logged out successfully.")
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        super.onCleared()
    }
}

class AgentDashboardViewModelFactory(
    private val repository: AgentRepository,
    private val tokenManager: AgentTokenManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgentDashboardViewModel::class.java)) {
            return AgentDashboardViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.washgo.agent.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.washgo.agent.data.local.AgentTokenManager
import com.washgo.agent.data.model.AgentProfileDto
import com.washgo.agent.data.model.RegisterAgentOtpRequest
import com.washgo.agent.data.repository.AgentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AgentAuthStep {
    PHONE_INPUT,
    OTP_INPUT,
    ONBOARDING,
    VERIFICATION_STATUS
}

data class AgentAuthUiState(
    val step: AgentAuthStep = AgentAuthStep.PHONE_INPUT,
    val phoneInput: String = "",
    val otpInput: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val vehicleSpecialty: String = "BOTH",
    val waterCapacity: Int = 200,
    val hasPressureWasher: Boolean = true,
    val hasVacuumExtractor: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val resendCountdown: Int = 0,
    val debugOtp: String? = null,
    val registrationToken: String? = null,
    val agentProfile: AgentProfileDto? = null
) {
    val isPhoneValid: Boolean
        get() = phoneInput.length == 10 && phoneInput.matches(Regex("^[6-9]\\d{9}$"))

    val isOtpValid: Boolean
        get() = otpInput.length == 6 && otpInput.all { it.isDigit() }

    val formattedFullPhone: String
        get() = "+91$phoneInput"

    val maskedPhone: String
        get() {
            if (phoneInput.length != 10) return "+91 $phoneInput"
            return "+91 ${phoneInput.substring(0, 2)}••• ••${phoneInput.substring(7)}"
        }

    val canResend: Boolean
        get() = resendCountdown <= 0 && !isLoading

    val isOnboardingValid: Boolean
        get() = firstName.isNotBlank() && lastName.isNotBlank() && email.contains("@") && email.contains(".")
}

class AgentAuthViewModel(
    private val repository: AgentRepository,
    private val tokenManager: AgentTokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentAuthUiState())
    val uiState: StateFlow<AgentAuthUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        // If already logged in, check verification status
        if (tokenManager.isLoggedIn.value) {
            val status = tokenManager.getAgentStatus()
            if (status.equals("APPROVED", ignoreCase = true)) {
                // Already approved
            } else {
                _uiState.update { it.copy(step = AgentAuthStep.VERIFICATION_STATUS) }
                refreshProfile()
            }
        }
    }

    fun onPhoneChanged(input: String) {
        val filtered = input.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(phoneInput = filtered, errorMessage = null) }
    }

    fun onOtpChanged(input: String) {
        val filtered = input.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(otpInput = filtered, errorMessage = null) }
        if (filtered.length == 6) {
            verifyOtp()
        }
    }

    fun onFirstNameChanged(name: String) {
        _uiState.update { it.copy(firstName = name, errorMessage = null) }
    }

    fun onLastNameChanged(name: String) {
        _uiState.update { it.copy(lastName = name, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onSpecialtyChanged(specialty: String) {
        _uiState.update { it.copy(vehicleSpecialty = specialty) }
    }

    fun onEquipmentChanged(water: Int, washer: Boolean, vacuum: Boolean) {
        _uiState.update {
            it.copy(
                waterCapacity = water,
                hasPressureWasher = washer,
                hasVacuumExtractor = vacuum
            )
        }
    }

    fun sendOtp() {
        val state = _uiState.value
        if (!state.isPhoneValid || state.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.sendOtp(state.formattedFullPhone)
            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            step = AgentAuthStep.OTP_INPUT,
                            isLoading = false,
                            otpInput = "",
                            errorMessage = null,
                            debugOtp = response.debugOtp,
                            successMessage = "OTP sent to ${state.maskedPhone}"
                        )
                    }
                    startCountdown(response.resendAfterSeconds.toInt().coerceAtLeast(30))
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to send OTP. Please try again."
                        )
                    }
                }
            )
        }
    }

    fun verifyOtp() {
        val state = _uiState.value
        if (!state.isOtpValid || state.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.verifyOtp(state.formattedFullPhone, state.otpInput)
            result.fold(
                onSuccess = { response ->
                    if (response.isRegistered) {
                        val status = response.agentProfile?.status ?: "PENDING_VERIFICATION"
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                agentProfile = response.agentProfile,
                                step = AgentAuthStep.VERIFICATION_STATUS,
                                successMessage = "Welcome back, ${response.agentProfile?.displayName}!"
                            )
                        }
                    } else {
                        // Phone verified! Store registration token and proceed to Onboarding registration
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                step = AgentAuthStep.ONBOARDING,
                                registrationToken = response.registrationToken,
                                successMessage = "Mobile number verified. Complete partner registration."
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otpInput = "",
                            errorMessage = error.message ?: "Verification failed. Please check the code."
                        )
                    }
                }
            )
        }
    }

    fun submitRegistration() {
        val state = _uiState.value
        if (!state.isOnboardingValid || state.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val req = RegisterAgentOtpRequest(
                phone = state.formattedFullPhone,
                registrationToken = state.registrationToken ?: "",
                firstName = state.firstName.trim(),
                lastName = state.lastName.trim(),
                email = state.email.trim(),
                vehicleSpecialty = state.vehicleSpecialty,
                waterCapacityLiters = state.waterCapacity,
                hasPressureWasher = state.hasPressureWasher,
                hasVacuumExtractor = state.hasVacuumExtractor
            )
            val result = repository.registerAgent(req)
            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = AgentAuthStep.VERIFICATION_STATUS,
                            agentProfile = response.agentProfile,
                            successMessage = "Application submitted successfully!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Registration failed. Please check inputs."
                        )
                    }
                }
            )
        }
    }

    fun submitForReview() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.submitForReview()
            result.fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            agentProfile = profile,
                            successMessage = "Your profile is under review by Wash-Go administrators."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to submit for review."
                        )
                    }
                }
            )
        }
    }

    fun refreshProfile() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.getProfile()
            result.fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            agentProfile = profile
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Could not refresh profile."
                        )
                    }
                }
            )
        }
    }

    fun navigateBackToPhone() {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                step = AgentAuthStep.PHONE_INPUT,
                otpInput = "",
                errorMessage = null,
                resendCountdown = 0
            )
        }
    }

    fun setAuditStep(step: AgentAuthStep, phone: String = "9876543210") {
        _uiState.update {
            it.copy(
                step = step,
                phoneInput = phone,
                debugOtp = "123456",
                resendCountdown = 30,
                firstName = "Vikram",
                lastName = "Singh",
                email = "vikram.singh@washgo.in"
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AgentAuthUiState()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        _uiState.update { it.copy(resendCountdown = seconds) }
        countdownJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(resendCountdown = remaining) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}

class AgentAuthViewModelFactory(
    private val repository: AgentRepository,
    private val tokenManager: AgentTokenManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgentAuthViewModel::class.java)) {
            return AgentAuthViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.washgo.user.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.washgo.user.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthStep {
    PHONE_INPUT,
    OTP_INPUT
}

data class AuthUiState(
    val step: AuthStep = AuthStep.PHONE_INPUT,
    val phoneInput: String = "",
    val otpInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val resendCountdown: Int = 0,
    val debugOtp: String? = null
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
}

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

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
                            step = AuthStep.OTP_INPUT,
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

    fun resendOtp() {
        val state = _uiState.value
        if (!state.canResend || state.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.sendOtp(state.formattedFullPhone)
            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otpInput = "",
                            errorMessage = null,
                            debugOtp = response.debugOtp,
                            successMessage = "OTP resent successfully"
                        )
                    }
                    startCountdown(response.resendAfterSeconds.toInt().coerceAtLeast(30))
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to resend OTP. Please wait."
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
                onSuccess = {
                    countdownJob?.cancel()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otpInput = "",
                            errorMessage = error.message ?: "Invalid OTP. Please try again."
                        )
                    }
                }
            )
        }
    }

    fun backToPhoneInput() {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                step = AuthStep.PHONE_INPUT,
                otpInput = "",
                errorMessage = null,
                debugOtp = null
            )
        }
    }

    fun setAuditOtpStep(phone: String = "9876543210") {
        _uiState.update {
            it.copy(
                step = AuthStep.OTP_INPUT,
                phoneInput = phone,
                debugOtp = "123456",
                resendCountdown = 30
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState()
        }
    }

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        _uiState.update { it.copy(resendCountdown = seconds) }
        countdownJob = viewModelScope.launch {
            var current = seconds
            while (current > 0) {
                delay(1000)
                current--
                _uiState.update { it.copy(resendCountdown = current) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repository) as T
    }
}

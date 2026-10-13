package com.example.waggoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waggoapp.data.network.dto.ProfileResponse
import com.example.waggoapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AuthNavigationEvent {
    data class AuthSuccess(val profile: ProfileResponse) : AuthNavigationEvent
    data class AuthFailed(val errorMessage: String) : AuthNavigationEvent
}

data class AuthState(
    val isSignUp: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val rememberMe: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthNavigationEvent>()
    val events: SharedFlow<AuthNavigationEvent> = _events.asSharedFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn

    fun toggleAuthMode(isSignUp: Boolean) {
        _uiState.update { it.copy(isSignUp = isSignUp, errorMessage = null) }
    }

    fun updateFullName(name: String) {
        _uiState.update { it.copy(fullName = name, errorMessage = null) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone, errorMessage = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun updateRememberMe(remember: Boolean) {
        _uiState.update { it.copy(rememberMe = remember) }
    }

    fun submit() {
        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password

        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email address is required") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Password is required") }
            return
        }

        if (state.isSignUp) {
            val trimmedName = state.fullName.trim()
            if (trimmedName.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Full name is required") }
                return
            }

            // Backend password requirements: 12-72 chars, upper, lower, number, symbol
            val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{12,72}$")
            if (!passwordPattern.matches(password)) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Password must be at least 12 characters and include uppercase, lowercase, a number, and a symbol (e.g. WashGo#2026!Secured)"
                    )
                }
                return
            }

            val nameParts = trimmedName.split(" ", limit = 2)
            val firstName = nameParts[0].trim()
            val lastName = if (nameParts.size > 1) nameParts[1].trim() else firstName

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            viewModelScope.launch {
                val result = authRepository.register(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password
                )
                _uiState.update { it.copy(isLoading = false) }
                result.fold(
                    onSuccess = { profile ->
                        _events.emit(AuthNavigationEvent.AuthSuccess(profile))
                    },
                    onFailure = { err ->
                        val msg = err.message ?: "Registration failed"
                        _uiState.update { it.copy(errorMessage = msg) }
                        _events.emit(AuthNavigationEvent.AuthFailed(msg))
                    }
                )
            }
        } else {
            // Login flow
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            viewModelScope.launch {
                val result = authRepository.login(email = email, password = password)
                _uiState.update { it.copy(isLoading = false) }
                result.fold(
                    onSuccess = { profile ->
                        _events.emit(AuthNavigationEvent.AuthSuccess(profile))
                    },
                    onFailure = { err ->
                        val msg = err.message ?: "Authentication failed"
                        _uiState.update { it.copy(errorMessage = msg) }
                        _events.emit(AuthNavigationEvent.AuthFailed(msg))
                    }
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

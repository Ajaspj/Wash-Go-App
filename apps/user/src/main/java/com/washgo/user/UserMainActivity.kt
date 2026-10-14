package com.washgo.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.washgo.core.designsystem.theme.RapidoTheme
import com.washgo.user.data.local.TokenManager
import com.washgo.user.data.network.ApiClient
import com.washgo.user.data.repository.AuthRepository
import com.washgo.user.ui.auth.AuthStep
import com.washgo.user.ui.auth.AuthViewModel
import com.washgo.user.ui.auth.AuthViewModelFactory
import com.washgo.user.ui.auth.LoginScreen
import com.washgo.user.ui.auth.OtpVerificationScreen
import com.washgo.user.ui.booking.BookingScreen

import com.washgo.user.data.repository.RealAuthRepository

class UserMainActivity : ComponentActivity() {

    private val tokenManager by lazy {
        TokenManager.getInstance(applicationContext)
    }

    private val authRepository: AuthRepository by lazy {
        RealAuthRepository(ApiClient.authApi, tokenManager)
    }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (com.washgo.user.BuildConfig.DEBUG) {
            handleAuditIntent(intent)
        }

        setContent {
            RapidoTheme {
                val isLoggedIn by tokenManager.isLoggedIn.collectAsState()
                val authUiState by authViewModel.uiState.collectAsState()

                AnimatedContent(
                    targetState = isLoggedIn,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "SessionBoundaryAnimation"
                ) { loggedIn ->
                    if (loggedIn) {
                        BookingScreen(
                            onLogout = { authViewModel.logout() },
                            userPhone = tokenManager.getUserPhone(),
                            userName = tokenManager.getUserFullName()
                        )
                    } else {
                        when (authUiState.step) {
                            AuthStep.PHONE_INPUT -> {
                                LoginScreen(
                                    uiState = authUiState,
                                    onPhoneChanged = { authViewModel.onPhoneChanged(it) },
                                    onContinueClicked = { authViewModel.sendOtp() },
                                    onDismissError = { authViewModel.dismissError() }
                                )
                            }
                            AuthStep.OTP_INPUT -> {
                                OtpVerificationScreen(
                                    uiState = authUiState,
                                    onOtpChanged = { authViewModel.onOtpChanged(it) },
                                    onVerifyClicked = { authViewModel.verifyOtp() },
                                    onResendClicked = { authViewModel.resendOtp() },
                                    onBackClicked = { authViewModel.backToPhoneInput() },
                                    onDismissError = { authViewModel.dismissError() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (com.washgo.user.BuildConfig.DEBUG) {
            handleAuditIntent(intent)
        }
    }

    private fun handleAuditIntent(intent: android.content.Intent?) {
        if (intent == null) return
        if (intent.getBooleanExtra("AUDIT_LOGOUT", false)) {
            tokenManager.clear()
        } else if (intent.getBooleanExtra("AUDIT_MOCK_LOGIN", false)) {
            tokenManager.saveUserSession(
                accessToken = "audit-access-token",
                refreshToken = "audit-refresh-token",
                phone = "+91 98765 43210",
                fullName = "Rahul Sharma",
                userId = "usr-audit-01"
            )
        } else if (intent.getStringExtra("AUDIT_STEP") == "OTP") {
            tokenManager.clear()
            authViewModel.setAuditOtpStep()
        }
    }
}

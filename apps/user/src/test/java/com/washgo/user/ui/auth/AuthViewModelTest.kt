package com.washgo.user.ui.auth

import com.washgo.user.data.network.AuthResponse
import com.washgo.user.data.network.SendOtpResponse
import com.washgo.user.data.network.UserProfile
import com.washgo.user.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = AuthViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isPhoneInputAndEmpty() {
        val state = viewModel.uiState.value
        assertEquals(AuthStep.PHONE_INPUT, state.step)
        assertEquals("", state.phoneInput)
        assertEquals("", state.otpInput)
        assertFalse(state.isPhoneValid)
        assertFalse(state.isOtpValid)
        assertNull(state.errorMessage)
    }

    @Test
    fun phoneValidation_acceptsOnlyValid10DigitIndianNumbers() {
        // Less than 10 digits
        viewModel.onPhoneChanged("98765")
        assertFalse(viewModel.uiState.value.isPhoneValid)

        // 10 digits but invalid starting digit (starts with 1)
        viewModel.onPhoneChanged("1234567890")
        assertFalse(viewModel.uiState.value.isPhoneValid)

        // Valid Indian numbers starting with 6, 7, 8, 9
        viewModel.onPhoneChanged("9876543210")
        assertTrue(viewModel.uiState.value.isPhoneValid)
        assertEquals("+919876543210", viewModel.uiState.value.formattedFullPhone)
        assertEquals("+91 98••• ••210", viewModel.uiState.value.maskedPhone)

        viewModel.onPhoneChanged("6123456789")
        assertTrue(viewModel.uiState.value.isPhoneValid)

        // Filters out non-digits and truncates beyond 10
        viewModel.onPhoneChanged("987abc654-321099")
        assertEquals("9876543210", viewModel.uiState.value.phoneInput)
        assertEquals(10, viewModel.uiState.value.phoneInput.length)
    }

    @Test
    fun sendOtp_success_navigatesToOtpStepAndStartsCountdown() = runTest {
        viewModel.onPhoneChanged("9876543210")
        viewModel.sendOtp()
        // Advance time to allow sendOtp coroutine to complete and start countdown
        testDispatcher.scheduler.advanceTimeBy(100)

        val stateDuringCountdown = viewModel.uiState.value
        assertEquals(AuthStep.OTP_INPUT, stateDuringCountdown.step)
        assertFalse(stateDuringCountdown.isLoading)
        assertNull(stateDuringCountdown.errorMessage)
        assertEquals("123456", stateDuringCountdown.debugOtp)
        assertEquals(30, stateDuringCountdown.resendCountdown)
        assertFalse(stateDuringCountdown.canResend)

        // Advance through the full 30 seconds
        testDispatcher.scheduler.advanceTimeBy(31_000)
        val stateAfterCountdown = viewModel.uiState.value
        assertEquals(0, stateAfterCountdown.resendCountdown)
        assertTrue(stateAfterCountdown.canResend)
    }

    @Test
    fun sendOtp_failure_displaysErrorMessageOnPhoneStep() = runTest {
        fakeRepository.shouldSendOtpSucceed = false
        fakeRepository.errorMessage = "Please enter a valid 10-digit Indian mobile number"

        viewModel.onPhoneChanged("9876543210")
        viewModel.sendOtp()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AuthStep.PHONE_INPUT, state.step)
        assertFalse(state.isLoading)
        assertEquals("Please enter a valid 10-digit Indian mobile number", state.errorMessage)
    }

    @Test
    fun verifyOtp_success_authenticatesUser() = runTest {
        viewModel.onPhoneChanged("9876543210")
        viewModel.sendOtp()
        testDispatcher.scheduler.advanceUntilIdle()

        // Entering 6 digits auto-submits verifyOtp()
        viewModel.onOtpChanged("123456")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertTrue(fakeRepository.isLoggedIn.value)
    }

    @Test
    fun verifyOtp_failure_showsErrorAndClearsOtpInput() = runTest {
        viewModel.onPhoneChanged("9876543210")
        viewModel.sendOtp()
        testDispatcher.scheduler.advanceUntilIdle()

        fakeRepository.shouldVerifyOtpSucceed = false
        fakeRepository.errorMessage = "Invalid OTP. 4 attempts remaining."

        viewModel.onOtpChanged("999999")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AuthStep.OTP_INPUT, state.step)
        assertFalse(state.isLoading)
        assertEquals("Invalid OTP. 4 attempts remaining.", state.errorMessage)
        assertEquals("", state.otpInput)
    }

    @Test
    fun backToPhoneInput_resetsToPhoneStep() = runTest {
        viewModel.onPhoneChanged("9876543210")
        viewModel.sendOtp()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AuthStep.OTP_INPUT, viewModel.uiState.value.step)

        viewModel.backToPhoneInput()

        val state = viewModel.uiState.value
        assertEquals(AuthStep.PHONE_INPUT, state.step)
        assertEquals("9876543210", state.phoneInput)
        assertEquals("", state.otpInput)
        assertNull(state.errorMessage)
    }

    @Test
    fun logout_clearsSession() = runTest {
        fakeRepository.setLoggedIn(true)
        assertTrue(fakeRepository.isLoggedIn.value)

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(fakeRepository.isLoggedIn.value)
        assertEquals(AuthStep.PHONE_INPUT, viewModel.uiState.value.step)
    }

    private class FakeAuthRepository : AuthRepository {
        private val _isLoggedIn = MutableStateFlow(false)
        override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

        var shouldSendOtpSucceed = true
        var shouldVerifyOtpSucceed = true
        var errorMessage = "Something went wrong"

        fun setLoggedIn(loggedIn: Boolean) {
            _isLoggedIn.value = loggedIn
        }

        override fun getStoredPhone(): String? = "+919876543210"
        override fun getStoredUserName(): String? = "Test User"
        override fun getStoredEmail(): String? = "test@customer.washgo.com"

        override suspend fun sendOtp(phone: String): Result<SendOtpResponse> {
            return if (shouldSendOtpSucceed) {
                Result.success(
                    SendOtpResponse(
                        phone = phone,
                        expiresInSeconds = 300,
                        resendAfterSeconds = 30,
                        message = "OTP sent",
                        debugOtp = "123456"
                    )
                )
            } else {
                Result.failure(Exception(errorMessage))
            }
        }

        override suspend fun verifyOtp(phone: String, otp: String): Result<AuthResponse> {
            return if (shouldVerifyOtpSucceed) {
                _isLoggedIn.value = true
                Result.success(
                    AuthResponse(
                        accessToken = "mock-access-token",
                        refreshToken = "mock-refresh-token",
                        tokenType = "Bearer",
                        expiresInSeconds = 3600
                    )
                )
            } else {
                Result.failure(Exception(errorMessage))
            }
        }

        override suspend fun fetchProfile(): Result<UserProfile> {
            return Result.success(
                UserProfile(
                    id = "mock-user-id",
                    name = "Test User",
                    email = "test@customer.washgo.com",
                    phone = "+919876543210",
                    profileImageUrl = null,
                    preferredAddress = "Indiranagar",
                    role = "CUSTOMER",
                    status = "ACTIVE"
                )
            )
        }

        override suspend fun logout(): Result<Unit> {
            _isLoggedIn.value = false
            return Result.success(Unit)
        }
    }
}

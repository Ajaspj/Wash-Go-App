package com.washgo.agent.ui.auth

import com.washgo.agent.data.local.AgentTokenManager
import com.washgo.agent.data.model.AgentAuthResponse
import com.washgo.agent.data.model.AgentProfileDto
import com.washgo.agent.data.model.RegisterAgentOtpRequest
import com.washgo.agent.data.repository.AgentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AgentAuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_defaultsToPhoneInput() {
        val state = AgentAuthUiState()
        assertEquals(AgentAuthStep.PHONE_INPUT, state.step)
        assertEquals("", state.phoneInput)
        assertEquals("", state.otpInput)
        assertFalse(state.isPhoneValid)
        assertFalse(state.isOtpValid)
        assertNull(state.errorMessage)
    }

    @Test
    fun phoneValidation_acceptsOnlyValid10DigitIndianNumbers() {
        var state = AgentAuthUiState(phoneInput = "9876543210")
        assertTrue(state.isPhoneValid)
        assertEquals("+919876543210", state.formattedFullPhone)

        // Starting with 5 is invalid
        state = AgentAuthUiState(phoneInput = "5123456789")
        assertFalse(state.isPhoneValid)

        // 9 digits is invalid
        state = AgentAuthUiState(phoneInput = "987654321")
        assertFalse(state.isPhoneValid)

        // 11 digits is invalid
        state = AgentAuthUiState(phoneInput = "98765432101")
        assertFalse(state.isPhoneValid)
    }

    @Test
    fun otpValidation_acceptsExactly6Digits() {
        var state = AgentAuthUiState(otpInput = "123456")
        assertTrue(state.isOtpValid)

        state = AgentAuthUiState(otpInput = "12345")
        assertFalse(state.isOtpValid)

        state = AgentAuthUiState(otpInput = "1234567")
        assertFalse(state.isOtpValid)

        state = AgentAuthUiState(otpInput = "12345a")
        assertFalse(state.isOtpValid)
    }

    @Test
    fun onboardingValidation_checksRequiredFields() {
        var state = AgentAuthUiState(
            firstName = "Rajesh",
            lastName = "Kumar",
            email = "rajesh@washgo.in"
        )
        assertTrue(state.isOnboardingValid)

        state = state.copy(firstName = "")
        assertFalse(state.isOnboardingValid)

        state = state.copy(firstName = "Rajesh", email = "invalid-email")
        assertFalse(state.isOnboardingValid)
    }

    @Test
    fun maskedPhone_formatsCorrectly() {
        val state = AgentAuthUiState(phoneInput = "9876543210")
        assertEquals("+91 98••• ••210", state.maskedPhone)
    }
}

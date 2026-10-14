package com.washgo.agent.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AgentDashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AgentDashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AgentDashboardViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaultState_isOfflineAndNoActiveJob() {
        assertFalse(viewModel.isOnline.value)
        assertNull(viewModel.incomingJob.value)
        assertNull(viewModel.activeJob.value)
        assertEquals(15, viewModel.countdownSeconds.value)
    }

    @Test
    fun declineJob_clearsIncomingJob() {
        viewModel.declineJob()
        assertNull(viewModel.incomingJob.value)
    }

    @Test
    fun finishActiveJob_clearsActiveJobAndStatus() {
        viewModel.finishActiveJob()
        assertNull(viewModel.activeJob.value)
        assertNull(viewModel.latestStatusUpdate.value)
    }
}

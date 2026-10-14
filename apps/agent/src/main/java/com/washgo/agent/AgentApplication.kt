package com.washgo.agent

import android.app.Application
import com.washgo.agent.data.local.AgentTokenManager
import com.washgo.agent.data.network.AgentApiClient
import com.washgo.agent.data.repository.AgentRepository

class AgentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize singletons
        AgentTokenManager.getInstance(this)
        AgentApiClient.getInstance(this)
        AgentRepository.getInstance(this)
    }
}

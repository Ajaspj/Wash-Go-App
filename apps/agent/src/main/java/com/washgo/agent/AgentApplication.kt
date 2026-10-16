package com.washgo.agent

import android.app.Application
import com.washgo.agent.data.local.AgentTokenManager
import com.washgo.agent.data.network.AgentApiClient
import com.washgo.agent.data.repository.AgentRepository
import org.maplibre.android.MapLibre

class AgentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        // Initialize singletons
        AgentTokenManager.getInstance(this)
        AgentApiClient.getInstance(this)
        AgentRepository.getInstance(this)
    }
}

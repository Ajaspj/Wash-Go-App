package com.washgo.user

import android.app.Application
import com.washgo.user.data.local.TokenManager
import com.washgo.user.data.network.ApiClient
import org.maplibre.android.MapLibre

class UserApplication : Application() {
    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        tokenManager = TokenManager.getInstance(this)
        ApiClient.initialize(tokenManager)
        com.washgo.user.data.network.UserWebSocketClient.tokenProvider = { tokenManager.getAccessToken() }
    }
}

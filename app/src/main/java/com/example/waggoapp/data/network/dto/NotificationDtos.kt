package com.example.waggoapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("body")
    val body: String,

    @SerializedName("read")
    val read: Boolean,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("readAt")
    val readAt: String?
)

data class DeviceTokenRequest(
    @SerializedName("token")
    val token: String,

    @SerializedName("platform")
    val platform: String = "ANDROID"
)

data class DeviceTokenResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("platform")
    val platform: String,

    @SerializedName("active")
    val active: Boolean,

    @SerializedName("lastSeenAt")
    val lastSeenAt: String?
)

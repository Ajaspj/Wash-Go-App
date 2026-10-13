package com.example.waggoapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class AddressRequest(
    @SerializedName("label")
    val label: String,

    @SerializedName("recipientName")
    val recipientName: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("addressLine1")
    val addressLine1: String,

    @SerializedName("addressLine2")
    val addressLine2: String? = null,

    @SerializedName("landmark")
    val landmark: String? = null,

    @SerializedName("city")
    val city: String,

    @SerializedName("state")
    val state: String,

    @SerializedName("postalCode")
    val postalCode: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("defaultAddress")
    val defaultAddress: Boolean = false
)

data class AddressResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("label")
    val label: String,

    @SerializedName("recipientName")
    val recipientName: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("addressLine1")
    val addressLine1: String,

    @SerializedName("addressLine2")
    val addressLine2: String?,

    @SerializedName("landmark")
    val landmark: String?,

    @SerializedName("city")
    val city: String,

    @SerializedName("state")
    val state: String,

    @SerializedName("postalCode")
    val postalCode: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("longitude")
    val longitude: Double?,

    @SerializedName("defaultAddress")
    val defaultAddress: Boolean,

    @SerializedName("active")
    val active: Boolean,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class ServiceAreaCheckResponse(
    @SerializedName("serviceable")
    val serviceable: Boolean,

    @SerializedName("distanceMeters")
    val distanceMeters: Double?,

    @SerializedName("serviceAreaId")
    val serviceAreaId: String?,

    @SerializedName("serviceAreaName")
    val serviceAreaName: String?
)

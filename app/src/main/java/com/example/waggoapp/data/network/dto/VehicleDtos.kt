package com.example.waggoapp.data.network.dto

import com.google.gson.annotations.SerializedName

enum class VehicleType {
    HATCHBACK,
    SEDAN,
    SUV,
    MUV,
    LUXURY,
    PICKUP,
    VAN,
    OTHER
}

data class CreateVehicleRequest(
    @SerializedName("vehicleType")
    val vehicleType: VehicleType,

    @SerializedName("make")
    val make: String,

    @SerializedName("model")
    val model: String,

    @SerializedName("year")
    val year: Int,

    @SerializedName("registrationNumber")
    val registrationNumber: String,

    @SerializedName("color")
    val color: String,

    @SerializedName("nickname")
    val nickname: String? = null,

    @SerializedName("isDefault")
    val isDefault: Boolean = false
)

data class UpdateVehicleRequest(
    @SerializedName("vehicleType")
    val vehicleType: VehicleType,

    @SerializedName("make")
    val make: String,

    @SerializedName("model")
    val model: String,

    @SerializedName("year")
    val year: Int,

    @SerializedName("registrationNumber")
    val registrationNumber: String,

    @SerializedName("color")
    val color: String,

    @SerializedName("nickname")
    val nickname: String? = null,

    @SerializedName("isDefault")
    val isDefault: Boolean = false
)

data class VehicleResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("vehicleType")
    val vehicleType: VehicleType,

    @SerializedName("make")
    val make: String,

    @SerializedName("model")
    val model: String,

    @SerializedName("year")
    val year: Int,

    @SerializedName("registrationNumber")
    val registrationNumber: String,

    @SerializedName("color")
    val color: String,

    @SerializedName("nickname")
    val nickname: String?,

    @SerializedName("isDefault")
    val isDefault: Boolean,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)

package com.example.waggoapp.data.network.dto

import com.google.gson.annotations.SerializedName

enum class ServiceCategory {
    EXTERIOR,
    INTERIOR,
    FULL_WASH,
    DETAILING,
    WATERLESS,
    SPECIALTY
}

data class WashServiceResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("category")
    val category: ServiceCategory,

    @SerializedName("shortDescription")
    val shortDescription: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("durationMinutes")
    val durationMinutes: Int,

    @SerializedName("basePrice")
    val basePrice: Double,

    @SerializedName("active")
    val active: Boolean,

    @SerializedName("displayOrder")
    val displayOrder: Int,

    @SerializedName("vehicleTypes")
    val vehicleTypes: Set<VehicleType> = emptySet(),

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class AddonResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("price")
    val price: Double,

    @SerializedName("active")
    val active: Boolean,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)

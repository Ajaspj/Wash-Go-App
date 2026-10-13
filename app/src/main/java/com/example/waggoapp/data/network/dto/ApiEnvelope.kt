package com.example.waggoapp.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Standard backend response wrapper matching Spring Boot ApiResponse<T>
 */
data class ApiResponse<T>(
    @SerializedName("data")
    val data: T
)

/**
 * Spring Boot Page response wrapper matching Spring Data Page<T>
 */
data class PageResponse<T>(
    @SerializedName("content")
    val content: List<T> = emptyList(),

    @SerializedName("page")
    val page: Int = 0,

    @SerializedName("size")
    val size: Int = 20,

    @SerializedName("totalElements")
    val totalElements: Long = 0,

    @SerializedName("totalPages")
    val totalPages: Int = 0,

    @SerializedName("first")
    val first: Boolean = true,

    @SerializedName("last")
    val last: Boolean = true
)

/**
 * Standard backend ErrorResponse matching GlobalExceptionHandler
 */
data class ApiErrorResponse(
    @SerializedName("timestamp")
    val timestamp: String? = null,

    @SerializedName("status")
    val status: Int = 400,

    @SerializedName("code")
    val code: String = "ERROR",

    @SerializedName("message")
    val message: String = "An unexpected error occurred",

    @SerializedName("fieldErrors")
    val fieldErrors: Map<String, String>? = null
)

package com.example.waggoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waggoapp.data.network.dto.BookingResponse
import com.example.waggoapp.data.network.dto.BookingStatus
import com.example.waggoapp.data.network.dto.ReviewResponse
import com.example.waggoapp.data.network.dto.TrackingResponse
import com.example.waggoapp.data.repository.BookingRepository
import com.example.waggoapp.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackingUiState(
    val bookingId: String = "",
    val bookingReference: String = "",
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val scheduledStartTime: String? = null,
    val scheduledEndTime: String? = null,
    val staffDisplayName: String? = null,
    val staffCode: String? = null,
    val assignedAt: String? = null,
    val acceptedAt: String? = null,
    val enRouteAt: String? = null,
    val arrivedAt: String? = null,
    val washingStartedAt: String? = null,
    val completedAt: String? = null,
    val cancellationReason: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val existingReview: ReviewResponse? = null,
    val isReviewSubmitted: Boolean = false,
    val showReviewDialog: Boolean = false
) {
    val isCancellable: Boolean
        get() = status.isCancellable()

    val isCompleted: Boolean
        get() = status == BookingStatus.COMPLETED

    val headlineText: String
        get() = when (status) {
            BookingStatus.PENDING -> "Appointment Received • Awaiting Confirmation"
            BookingStatus.CONFIRMED -> "Booking Confirmed • Dispatching Detail Van"
            BookingStatus.ASSIGNED -> "Detail Crew Assigned • Preparing Eco Rig"
            BookingStatus.STAFF_ACCEPTED -> "Partner Accepted • Loading Premium Supplies"
            BookingStatus.EN_ROUTE -> "Wash Partner En Route • Arriving Soon"
            BookingStatus.ARRIVED -> "Partner Arrived • Setting Up at Location"
            BookingStatus.IN_PROGRESS -> "Detail Spa In Progress • Closed-Loop Wash"
            BookingStatus.COMPLETED -> "Wash Completed • 7-Point Inspection Passed"
            BookingStatus.CANCELLED -> "Appointment Cancelled"
            BookingStatus.NO_SHOW -> "Marked No-Show by Operator"
        }
}

class TrackingViewModel(
    private val bookingRepository: BookingRepository = BookingRepository(),
    private val reviewRepository: ReviewRepository = ReviewRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(TrackingUiState())
    val state: StateFlow<TrackingUiState> = _state.asStateFlow()

    fun loadTracking(bookingId: String, reference: String? = null) {
        _state.update {
            it.copy(
                bookingId = bookingId,
                bookingReference = reference ?: it.bookingReference,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val trackingRes = bookingRepository.getBookingTracking(bookingId)
            trackingRes.fold(
                onSuccess = { tracking ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            bookingReference = tracking.bookingReference,
                            status = tracking.status,
                            scheduledStartTime = tracking.scheduledStartTime,
                            scheduledEndTime = tracking.scheduledEndTime,
                            staffDisplayName = tracking.assignedStaff?.displayName,
                            staffCode = tracking.assignedStaff?.staffCode,
                            assignedAt = tracking.assignedAt,
                            acceptedAt = tracking.acceptedAt,
                            enRouteAt = tracking.enRouteAt,
                            arrivedAt = tracking.arrivedAt,
                            washingStartedAt = tracking.washingStartedAt,
                            completedAt = tracking.completedAt
                        )
                    }

                    // Check if review exists if completed
                    if (tracking.status == BookingStatus.COMPLETED) {
                        val reviewRes = reviewRepository.getBookingReview(bookingId)
                        reviewRes.getOrNull()?.let { review ->
                            _state.update {
                                it.copy(
                                    existingReview = review,
                                    isReviewSubmitted = true
                                )
                            }
                        }
                    }
                },
                onFailure = { err ->
                    // Fallback to fetch booking details by ID
                    val bookingRes = bookingRepository.getBookingById(bookingId)
                    bookingRes.fold(
                        onSuccess = { b ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    bookingReference = b.bookingReference,
                                    status = b.status,
                                    scheduledStartTime = b.scheduledStartTime,
                                    scheduledEndTime = b.scheduledEndTime,
                                    staffDisplayName = b.assignedStaff?.displayName,
                                    staffCode = b.assignedStaff?.staffCode,
                                    assignedAt = b.assignedAt,
                                    acceptedAt = b.acceptedAt,
                                    enRouteAt = b.enRouteAt,
                                    arrivedAt = b.arrivedAt,
                                    washingStartedAt = b.washingStartedAt,
                                    completedAt = b.completedAt,
                                    cancellationReason = b.cancellationReason
                                )
                            }
                        },
                        onFailure = {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = err.message ?: "Failed to fetch live tracking"
                                )
                            }
                        }
                    )
                }
            )
        }
    }

    fun cancelBooking(reason: String? = null) {
        val bookingId = _state.value.bookingId
        if (bookingId.isBlank()) return

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = bookingRepository.cancelBooking(bookingId, reason)
            _state.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = { cancelled ->
                    _state.update {
                        it.copy(
                            status = cancelled.status,
                            cancellationReason = cancelled.cancellationReason
                        )
                    }
                },
                onFailure = { err ->
                    _state.update { it.copy(errorMessage = err.message ?: "Failed to cancel booking") }
                }
            )
        }
    }

    fun showReviewDialog(show: Boolean) {
        _state.update { it.copy(showReviewDialog = show) }
    }

    fun submitReview(rating: Int, comment: String?) {
        val bookingId = _state.value.bookingId
        if (bookingId.isBlank()) return

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = reviewRepository.submitReview(bookingId, rating, comment)
            _state.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = { review ->
                    _state.update {
                        it.copy(
                            existingReview = review,
                            isReviewSubmitted = true,
                            showReviewDialog = false
                        )
                    }
                },
                onFailure = { err ->
                    _state.update { it.copy(errorMessage = err.message ?: "Failed to submit review") }
                }
            )
        }
    }
}

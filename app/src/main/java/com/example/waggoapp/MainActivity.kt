package com.example.waggoapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.waggoapp.data.network.ApiClient
import com.example.waggoapp.data.network.dto.BookingResponse
import com.example.waggoapp.data.network.dto.PaymentOrderResponse
import com.example.waggoapp.ui.screens.HydroGlideApp
import com.example.waggoapp.ui.theme.HydroGlideTheme
import com.example.waggoapp.ui.viewmodel.BookingViewModel
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    private val bookingViewModel: BookingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ApiClient and preload Razorpay checkout
        ApiClient.init(applicationContext)
        Checkout.preload(applicationContext)

        setContent {
            HydroGlideTheme {
                HydroGlideApp(
                    bookingViewModel = bookingViewModel,
                    onLaunchRazorpay = { paymentOrder, booking ->
                        launchRazorpayCheckout(paymentOrder, booking)
                    }
                )
            }
        }
    }

    private fun launchRazorpayCheckout(
        paymentOrder: PaymentOrderResponse,
        booking: BookingResponse
    ) {
        val checkout = Checkout()
        checkout.setKeyID(paymentOrder.razorpayKeyId)

        try {
            val userEmail = ApiClient.getTokenManager().getUserEmail()
            val userName = ApiClient.getTokenManager().getUserFullName()

            val options = JSONObject().apply {
                put("name", "Wash-Go Mobile Detailing")
                put("description", booking.serviceNameSnapshot ?: "Appointment ${booking.bookingReference}")
                put("image", "https://img.icons8.com/isometric/512/car-wash.png")
                put("order_id", paymentOrder.razorpayOrderId)
                put("currency", paymentOrder.currency)
                // Amount in smallest currency unit (paise/cents)
                put("amount", (paymentOrder.amount * 100).toLong())

                val prefill = JSONObject().apply {
                    if (!userEmail.isNullOrBlank()) put("email", userEmail)
                    if (!userName.isNullOrBlank()) put("name", userName)
                }
                put("prefill", prefill)

                val theme = JSONObject().apply {
                    put("color", "#00F5D4")
                }
                put("theme", theme)
            }

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing Razorpay: ${e.message}", Toast.LENGTH_LONG).show()
            bookingViewModel.onRazorpayPaymentFailed(-1, e.message ?: "Failed to open payment gateway")
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val paymentId = razorpayPaymentId ?: paymentData?.paymentId
        val signature = paymentData?.signature

        if (!paymentId.isNullOrBlank() && !signature.isNullOrBlank()) {
            bookingViewModel.onRazorpayPaymentSuccess(
                razorpayPaymentId = paymentId,
                razorpaySignature = signature
            )
        } else {
            bookingViewModel.onRazorpayPaymentFailed(
                code = 0,
                description = "Missing payment verification parameters from gateway"
            )
        }
    }

    override fun onPaymentError(errorCode: Int, errorDescription: String?, paymentData: PaymentData?) {
        val description = errorDescription ?: "Payment was cancelled or rejected"
        bookingViewModel.onRazorpayPaymentFailed(
            code = errorCode,
            description = description
        )
    }
}

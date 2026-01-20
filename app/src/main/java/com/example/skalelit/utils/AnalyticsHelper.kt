package com.example.skalelit.utils

import com.example.skalelit.data.entity.BookingEntity
import com.example.skalelit.data.entity.UserEntity

object AnalyticsHelper {
    fun trackBookingCreated(booking: BookingEntity) {
        // TODO: Integrate with Firebase Analytics
        // FirebaseAnalytics.getInstance(context).logEvent("booking_created", bundleOf(
        //     "room_name" to booking.roomName,
        //     "cost" to booking.cost,
        //     "user_email" to booking.userEmail
        // ))
    }

    fun trackUserLogin(user: UserEntity) {
        // TODO: Track login event
    }
}
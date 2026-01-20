package com.example.skalelit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val userName: String,
    val roomName: String,
    val roomAddress: String,
    val roomImage: String,
    val date: String,
    val startTime: Long,
    val endTime: Long,
    val displayTime: String,
    val cost: Double,
    val roomId: Int,
    val status: String = "confirmed", // confirmed, cancelled, completed
    val paymentStatus: String = "paid", // paid, pending, failed
    val bookingId: String = generateBookingId(),
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun generateBookingId(): String = "BK${System.currentTimeMillis() % 1000000}"
    }
}
package com.example.skalelit.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val userEmail: String,
    val roomName: String,
    val roomAddress: String,
    val roomImage: String,
    val date: String,
    val startTime: Long,
    val endTime: Long,
    val displayTime: String
)
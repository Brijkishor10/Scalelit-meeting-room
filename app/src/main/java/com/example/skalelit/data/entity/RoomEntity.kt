package com.example.skalelit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val loc: String,
    val cap: Int,
    val img: String,
    val pricePerHour: Double = 50.0,
    val amenities: List<String> = emptyList(),
    val rating: Float = 4.5f,
    val isAvailable: Boolean = true,
    val description: String = "",
    val isFavorite: Boolean = false // <--- NEW FIELD
)
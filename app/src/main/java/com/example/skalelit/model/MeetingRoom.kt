package com.example.skalelit.model

data class MeetingRoom(
    val id: String,
    val name: String,
    val isBooked: Boolean,
    val statusText: String,
    val imageUrl: String,
    val capacity: Int
)
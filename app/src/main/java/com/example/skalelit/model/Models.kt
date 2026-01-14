package com.example.skalelit.model

data class AppData(
    val rooms: List<MeetingRoom>,
    val events: List<Event>
)

data class MeetingRoom(
    val id: String,
    val name: String,
    val isBooked: Boolean,
    val statusText: String,
    val imageUrl: String,
    val capacity: Int
)


data class Event(
    val id: String,
    val title: String,
    val location: String,
    val time: String,
    val imageUrl: String

)
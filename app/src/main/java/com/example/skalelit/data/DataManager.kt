package com.example.scalelit.data

import android.content.Context
import com.example.skalelit.model.AppData
import com.example.skalelit.model.Event
import com.example.skalelit.model.MeetingRoom

class DataManager(private val context: Context) {

    fun getDashboardData(): AppData {
        val rooms = listOf(
            MeetingRoom(
                id = "1",
                name = "Executive Boardroom",
                imageUrl = "https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=800&q=80",
                statusText = "Smart TV • Video Conf",
                isBooked = false,
                capacity = 12 // Large
            ),
            MeetingRoom(
                id = "2",
                name = "Creative Huddle",
                imageUrl = "https://images.unsplash.com/photo-1517502884422-41e157d2fc39?auto=format&fit=crop&w=800&q=80",
                statusText = "Whiteboard • 4 Seats",
                isBooked = true,
                capacity = 4 // Small
            ),
            MeetingRoom(
                id = "3",
                name = "Phone Booth A",
                imageUrl = "https://images.unsplash.com/photo-1588072432836-e10032774350?auto=format&fit=crop&w=800&q=80",
                statusText = "Soundproof • 1 Seat",
                isBooked = false,
                capacity = 1 // Small
            ),
            MeetingRoom(
                id = "4",
                name = "Training Room",
                imageUrl = "https://images.unsplash.com/photo-1504384764586-bb4cdc1707b0?auto=format&fit=crop&w=800&q=80",
                statusText = "Projector • 20 Seats",
                isBooked = false,
                capacity = 20 // Large
            )
        )
        val events = listOf(
            Event("1", "Town Hall", "Cafeteria", "2:00 PM", "https://images.unsplash.com/photo-1511578314322-379afb476865?auto=format&fit=crop&w=200&q=80"),
            Event("2", "Team Lunch", "Roof Garden", "1:00 PM", "https://images.unsplash.com/photo-1529333166437-7750a6dd5a70?auto=format&fit=crop&w=200&q=80")
        )

        return AppData(rooms, events)
    }
}
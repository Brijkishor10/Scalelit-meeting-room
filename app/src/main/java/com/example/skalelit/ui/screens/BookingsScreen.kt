package com.example.skalelit.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skalelit.data.entity.BookingEntity
import com.example.skalelit.ui.components.SkaleCard
import com.example.skalelit.ui.theme.SkalePrimary
import com.example.skalelit.ui.theme.SkaleTextHead
import com.example.skalelit.ui.viewmodel.MainViewModel
import java.util.Calendar

@Composable
fun BookingsScreen(vm: MainViewModel) {
    val bookings by vm.myBookings.collectAsState()
    var bookingToCancel by remember { mutableStateOf<BookingEntity?>(null) }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("My Schedule", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SkaleTextHead) }

        items(bookings) { booking ->
            BookingCard(
                booking = booking,
                onCancelClick = { bookingToCancel = booking }
            )
        }
    }

    if (bookingToCancel != null) {
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            title = { Text("Cancel Booking?") },
            text = { Text("Are you sure you want to cancel your reservation for ${bookingToCancel?.roomName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.cancelBooking(bookingToCancel!!)
                        bookingToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Yes, Cancel") }
            },
            dismissButton = {
                TextButton(onClick = { bookingToCancel = null }) { Text("No, Keep it") }
            }
        )
    }
}

@Composable
fun BookingCard(booking: BookingEntity, onCancelClick: () -> Unit) {
    val context = LocalContext.current
    val isCancellable = booking.status != "cancelled" && booking.status != "rejected"

    SkaleCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            // Header Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(booking.roomName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${booking.date} • ${booking.displayTime}", fontSize = 14.sp)
                }

                val (statusColor, textColor) = when(booking.status) {
                    "confirmed" -> Color(0xFF10B981).copy(0.1f) to Color(0xFF10B981)
                    "rejected", "cancelled" -> Color(0xFFEF4444).copy(0.1f) to Color(0xFFEF4444)
                    else -> Color(0xFFF59E0B).copy(0.1f) to Color(0xFFF59E0B)
                }

                Box(Modifier.clip(RoundedCornerShape(50)).background(statusColor).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(booking.status.uppercase(), color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action Buttons Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                // 1. CANCEL BUTTON
                if (isCancellable) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f).height(40.dp),
                        border = BorderStroke(1.dp, Color.Red.copy(0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Rounded.Cancel, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel", fontSize = 12.sp)
                    }
                }

                // 2. CALENDAR BUTTON (Only if Confirmed)
                if (booking.status == "confirmed") {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    data = CalendarContract.Events.CONTENT_URI
                                    putExtra(CalendarContract.Events.TITLE, "Meeting at ${booking.roomName}")
                                    putExtra(CalendarContract.Events.EVENT_LOCATION, booking.roomAddress)
                                    putExtra(CalendarContract.Events.DESCRIPTION, "Booked via Skalelit App")
                                    // Parse time (Simplified for demo)
                                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, booking.startTime)
                                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, booking.endTime)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback if no calendar app
                            }
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkalePrimary),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Rounded.CalendarToday, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Calendar", fontSize = 12.sp)
                    }
                }

                // 3. SHARE BUTTON (Only if Confirmed)
                if (booking.status == "confirmed") {
                    FilledTonalButton(
                        onClick = {
                            val shareText = "Meeting Confirmed!\nRoom: ${booking.roomName}\nTime: ${booking.date} ${booking.displayTime}\nLocation: ${booking.roomAddress}"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        },
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Rounded.Share, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
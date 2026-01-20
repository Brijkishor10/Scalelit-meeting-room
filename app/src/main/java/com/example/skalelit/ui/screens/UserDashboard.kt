package com.example.skalelit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.skalelit.data.entity.BookingEntity
import com.example.skalelit.data.entity.RoomEntity
import com.example.skalelit.data.entity.UserEntity
import com.example.skalelit.ui.components.GlassCard
import com.example.skalelit.ui.components.EmptyState // IMPORT THIS
import com.example.skalelit.ui.components.RoomSkeletonCard // IMPORT THIS
import com.example.skalelit.ui.theme.SkalePrimary
import com.example.skalelit.ui.theme.SkaleTextBody
import com.example.skalelit.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

val PremiumGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF4F46E5), Color(0xFF818CF8))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboard(vm: MainViewModel, user: UserEntity, onRoomClick: (RoomEntity) -> Unit) {
    val bookings by vm.myBookings.collectAsState()
    val rooms by vm.rooms.collectAsState()
    val isLoading by vm.isLoading.collectAsState() // Use ViewModel loading state

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    // Simulate local loading for UX (optional, looks nice on first load)
    var isLocalLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(800)
        isLocalLoading = false
    }

    val filteredRooms = rooms.filter { room ->
        val matchesSearch = room.name.contains(searchQuery, ignoreCase = true) || room.loc.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when(selectedFilter) {
            "All" -> true
            "Small" -> room.cap <= 6
            "Large" -> room.cap > 6
            "Projector" -> room.amenities.any { it.contains("Projector", ignoreCase = true) }
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val upcomingBookings = remember(bookings) {
        bookings.filter { it.status == "confirmed" && it.startTime > System.currentTimeMillis() }
            .sortedBy { it.startTime }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF1F5F9))) {
        // Header
        Box(Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)).background(PremiumGradient))

        Column {
            // Top Bar
            Row(Modifier.fillMaxWidth().padding(top = 50.dp, start = 24.dp, end = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Hello,", color = Color.White.copy(0.7f), fontSize = 16.sp)
                    Text(user.name.split(" ")[0], color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Surface(shape = CircleShape, color = Color.White.copy(0.2f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text(user.name.take(1), color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).background(Color.White, RoundedCornerShape(16.dp)),
                placeholder = { Text("Find your workspace...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = SkalePrimary) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
            )

            Spacer(Modifier.height(24.dp))

            // Main Content
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // SKELETON LOADING STATE
                if (isLocalLoading || isLoading) {
                    items(4) {
                        Box(Modifier.padding(horizontal = 24.dp)) {
                            RoomSkeletonCard()
                        }
                    }
                } else {
                    // REAL CONTENT
                    if (upcomingBookings.isNotEmpty()) {
                        item { Box(Modifier.padding(horizontal = 24.dp)) { NextMeetingCard(upcomingBookings.first()) } }
                    }

                    item {
                        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("All", "Small", "Large", "Projector").forEach { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter, onClick = { selectedFilter = filter },
                                    label = { Text(filter) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SkalePrimary, selectedLabelColor = Color.White, containerColor = Color.White),
                                    border = FilterChipDefaults.filterChipBorder(borderColor = if (selectedFilter == filter) Color.Transparent else Color.LightGray)
                                )
                            }
                        }
                    }

                    item {
                        Row(Modifier.padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ModernStatCard("Upcoming", upcomingBookings.size.toString(), Icons.Rounded.Event, Modifier.weight(1f))
                            ModernStatCard("Hours", calculateTotalHours(bookings).toString(), Icons.Rounded.Schedule, Modifier.weight(1f))
                        }
                    }

                    item { Text("Popular Rooms", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.padding(horizontal = 24.dp)) }

                    // EMPTY STATE
                    if (filteredRooms.isEmpty()) {
                        item {
                            EmptyState(
                                title = "No rooms found",
                                subtitle = "We couldn't find any rooms matching '$searchQuery'. Try a different keyword."
                            )
                        }
                    } else {
                        items(filteredRooms) { room ->
                            Box(Modifier.padding(horizontal = 24.dp)) {
                                ModernRoomCard(room, onFavClick = { vm.toggleFavorite(room) }, onClick = { onRoomClick(room) })
                            }
                        }
                    }
                }
            }
        }
    }
}

// ... (Keep existing ModernRoomCard, NextMeetingCard, ModernStatCard, Helper Functions)
// NOTE: Ensure you keep the helper components from the previous step here.
@Composable
fun ModernRoomCard(room: RoomEntity, onFavClick: () -> Unit, onClick: () -> Unit) {
    GlassCard(onClick = onClick) {
        Column {
            Box(Modifier.height(180.dp).fillMaxWidth()) {
                AsyncImage(model = room.img, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Surface(modifier = Modifier.padding(12.dp).align(Alignment.TopStart), shape = RoundedCornerShape(8.dp), color = Color.White.copy(0.9f)) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Text(" ${room.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(modifier = Modifier.padding(12.dp).align(Alignment.TopEnd).size(36.dp), shape = CircleShape, color = Color.White.copy(0.9f), onClick = onFavClick) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(if (room.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null, tint = if (room.isFavorite) Color.Red else Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(room.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text("$${room.pricePerHour}/hr", color = SkalePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(" ${room.loc}", fontSize = 13.sp, color = Color.Gray, maxLines = 1)
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    room.amenities.take(3).forEach {
                        Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp)) {
                            Text(it, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NextMeetingCard(booking: BookingEntity) {
    val timeUntil = remember(booking.startTime) { calculateTimeUntil(booking.startTime) }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Next Meeting", fontSize = 12.sp, color = SkaleTextBody)
                Text(booking.roomName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Starts in $timeUntil", color = SkalePrimary, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun ModernStatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    GlassCard(modifier) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = SkalePrimary.copy(0.1f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = SkalePrimary, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Text(title, fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }
    }
}

fun calculateTimeUntil(startTime: Long): String {
    val diff = startTime - System.currentTimeMillis()
    val hours = diff / (1000 * 60 * 60)
    val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

fun calculateTotalHours(bookings: List<BookingEntity>): Int = bookings.sumOf { (it.endTime - it.startTime) / (1000 * 60 * 60) }.toInt()
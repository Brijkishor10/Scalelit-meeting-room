package com.example.skalelit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.skalelit.data.entity.BookingEntity
import com.example.skalelit.data.entity.RoomEntity
import com.example.skalelit.data.entity.UserEntity
import com.example.skalelit.ui.components.SkaleButton
import com.example.skalelit.ui.components.SkaleCard
import com.example.skalelit.ui.components.SkaleChip
import com.example.skalelit.ui.components.SkaleInput
import com.example.skalelit.ui.theme.SkalePrimary
import com.example.skalelit.ui.theme.SkaleTextBody
import com.example.skalelit.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(vm: MainViewModel, user: UserEntity, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Bookings", "Rooms")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkalePrimary),
                actions = { IconButton(onClick = onLogout) { Icon(Icons.Rounded.Logout, null, tint = Color.White) } }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(when(index){
                                0 -> Icons.Rounded.Dashboard
                                1 -> Icons.Rounded.Book
                                else -> Icons.Rounded.MeetingRoom
                            }, null)
                        },
                        label = { Text(title) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = SkalePrimary, indicatorColor = SkalePrimary.copy(0.1f))
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> AdminOverview(vm)
                1 -> AdminBookings(vm)
                2 -> AdminRooms(vm)
            }
        }
    }
}

// --- TAB 1: OVERVIEW (ANALYTICS) ---
@Composable
fun AdminOverview(vm: MainViewModel) {
    val stats by vm.dashboardStats.collectAsState()
    val weeklyStats by vm.weeklyStats.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Dashboard Overview", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Total Rooms", stats.totalRooms.toString(), Icons.Rounded.MeetingRoom, SkalePrimary, Modifier.weight(1f))
                StatCard("Active Bookings", stats.activeBookings.toString(), Icons.Rounded.BookOnline, SkalePrimary, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Revenue", "$${stats.monthlyRevenue.toInt()}", Icons.Rounded.AttachMoney, Color(0xFF10B981), Modifier.weight(1f))
                StatCard("Users", stats.activeUsers.toString(), Icons.Rounded.People, Color(0xFFF59E0B), Modifier.weight(1f))
            }
        }

        item {
            SkaleCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Weekly Traffic", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(24.dp))
                    WeeklyChart(weeklyStats)
                }
            }
        }
    }
}

// --- TAB 2: BOOKINGS (APPROVE/REJECT) ---
@Composable
fun AdminBookings(vm: MainViewModel) {
    val bookings by vm.allBookings.collectAsState()

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Manage Bookings", fontWeight = FontWeight.Bold, fontSize = 24.sp) }

        items(bookings) { booking ->
            SkaleCard {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(booking.roomName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        SkaleChip(booking.status, booking.status == "confirmed")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("User: ${booking.userEmail}", fontSize = 12.sp, color = SkaleTextBody)
                    Text("Date: ${booking.date} (${booking.displayTime})", fontSize = 12.sp, color = SkaleTextBody)

                    if (booking.status == "pending") {
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { vm.updateBookingStatus(booking, "confirmed") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Approve") }

                            Button(
                                onClick = { vm.updateBookingStatus(booking, "rejected") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Reject") }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 3: ROOMS (ADD/DELETE) ---
@Composable
fun AdminRooms(vm: MainViewModel) {
    val rooms by vm.rooms.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Manage Rooms", fontWeight = FontWeight.Bold, fontSize = 24.sp) }

            items(rooms) { room ->
                SkaleCard {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = room.img, null, Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(room.name, fontWeight = FontWeight.Bold)
                            Text(room.loc, fontSize = 12.sp, color = SkaleTextBody, maxLines = 1)
                            Text("${room.cap} Seats • $${room.pricePerHour}/hr", fontSize = 12.sp, color = SkalePrimary)
                        }
                        IconButton(onClick = { vm.deleteRoom(room) }) {
                            Icon(Icons.Rounded.Delete, null, tint = Color.Red)
                        }
                    }
                }
            }
            // Add padding at bottom so FAB doesn't cover last item
            item { Spacer(Modifier.height(80.dp)) }
        }

        // --- FLOATING ACTION BUTTON TO ADD ROOM ---
        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = SkalePrimary,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) { Icon(Icons.Rounded.Add, null) }
    }

    if(showDialog) {
        AddRoomDialog(onDismiss = { showDialog = false }) { name, loc, cap, img, price ->
            vm.addRoom(name, loc, cap, img, price)
            showDialog = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoomDialog(onDismiss: () -> Unit, onAdd: (String, String, Int, String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("") }
    var cap by remember { mutableStateOf("") }
    var img by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Room", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Room Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = cap, onValueChange = { cap = it }, label = { Text("Capacity") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price/Hr") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                OutlinedTextField(value = img, onValueChange = { img = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("https://...") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if(name.isNotEmpty() && loc.isNotEmpty()) {
                        onAdd(name, loc, cap.toIntOrNull()?:0, img, price.toDoubleOrNull()?:0.0)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkalePrimary)
            ) { Text("Add Room") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// --- HELPER COMPONENT: STAT CARD ---
@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    SkaleCard(modifier) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 12.sp, color = SkaleTextBody)
        }
    }
}

// --- HELPER COMPONENT: WEEKLY CHART ---
@Composable
fun WeeklyChart(stats: List<com.example.skalelit.ui.viewmodel.MainViewModel.WeekDayStat>) {
    val maxCount = stats.maxOfOrNull { it.count } ?: 1
    Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        stats.forEach { stat ->
            val height = if (maxCount > 0) stat.count.toFloat() / maxCount else 0f
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.width(24.dp).fillMaxHeight(height.coerceAtLeast(0.02f)).clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).background(SkalePrimary))
                Spacer(Modifier.height(8.dp))
                Text(stat.day, fontSize = 12.sp, color = SkaleTextBody)
            }
        }
    }
}
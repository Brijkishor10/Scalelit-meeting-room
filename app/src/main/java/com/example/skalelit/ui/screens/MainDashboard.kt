package com.example.skalelit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.skalelit.data.entity.UserEntity
import com.example.skalelit.ui.viewmodel.MainViewModel
import com.example.skalelit.ui.theme.SkaleBackground
import com.example.skalelit.ui.theme.SkaleSurface
import com.example.skalelit.ui.theme.SkalePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(vm: MainViewModel, user: UserEntity) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedRoom by remember { mutableStateOf<com.example.skalelit.data.entity.RoomEntity?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    val context = LocalContext.current // For Toast

    Scaffold(
        bottomBar = { if(selectedRoom == null) BottomNavDock(selectedTab) { selectedTab = it } },
        containerColor = SkaleBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = "https://skalelit.com/assets/Skalelit__Logo_1_png-CDnnzTmQ.png", contentDescription = null, modifier = Modifier.height(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Skalelit", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkaleSurface),
                actions = { IconButton(onClick = {}) { Icon(Icons.Rounded.Notifications, null) } }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when(selectedTab) {
                0 -> UserDashboard(vm, user) { selectedRoom = it }
                1 -> BookingsScreen(vm)
                2 -> ProfileScreen(user) { vm.logout() }
            }
        }

        if (selectedRoom != null) {
            RoomDetail(
                room = selectedRoom!!,
                onDismiss = { selectedRoom = null },
                onBook = { date, timeSlot ->
                    // --- UPDATED CALL ---
                    vm.bookRoom(
                        room = selectedRoom!!,
                        date = date,
                        timeSlot = timeSlot,
                        cost = selectedRoom!!.pricePerHour,
                        onSuccess = {
                            showSuccess = true
                            selectedRoom = null
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }

        if (showSuccess) {
            SuccessDialog { showSuccess = false }
        }
    }
}

@Composable
fun BottomNavDock(selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = SkaleSurface, tonalElevation = 10.dp) {
        NavigationBarItem(selected = selected == 0, onClick = { onSelect(0) }, icon = { Icon(Icons.Rounded.Home, null) }, label = { Text("Home") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = SkalePrimary, indicatorColor = SkalePrimary.copy(alpha = 0.1f)))
        NavigationBarItem(selected = selected == 1, onClick = { onSelect(1) }, icon = { Icon(Icons.Rounded.CalendarMonth, null) }, label = { Text("Bookings") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = SkalePrimary, indicatorColor = SkalePrimary.copy(alpha = 0.1f)))
        NavigationBarItem(selected = selected == 2, onClick = { onSelect(2) }, icon = { Icon(Icons.Rounded.Person, null) }, label = { Text("Profile") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = SkalePrimary, indicatorColor = SkalePrimary.copy(alpha = 0.1f)))
    }
}
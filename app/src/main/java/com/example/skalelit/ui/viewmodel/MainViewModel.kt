package com.example.skalelit.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.skalelit.data.database.AppDatabase
import com.example.skalelit.data.entity.BookingEntity
import com.example.skalelit.data.entity.RoomEntity
import com.example.skalelit.data.entity.UserEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getDatabase(app)

    // --- STATE FLOWS ---
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _showOnboarding = MutableStateFlow(true)
    val showOnboarding = _showOnboarding.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _rooms = MutableStateFlow<List<RoomEntity>>(emptyList())
    val rooms = _rooms.asStateFlow()

    private val _myBookings = MutableStateFlow<List<BookingEntity>>(emptyList())
    val myBookings = _myBookings.asStateFlow()

    private val _allBookings = MutableStateFlow<List<BookingEntity>>(emptyList())
    val allBookings = _allBookings.asStateFlow()

    // Analytics Data Classes
    data class WeekDayStat(val day: String, val count: Int)
    data class DashboardStats(
        val totalRooms: Int = 0,
        val activeBookings: Int = 0,
        val monthlyRevenue: Double = 0.0,
        val activeUsers: Int = 0
    )

    private val _weeklyStats = MutableStateFlow<List<WeekDayStat>>(emptyList())
    val weeklyStats = _weeklyStats.asStateFlow()

    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats = _dashboardStats.asStateFlow()

    init {
        viewModelScope.launch {
            // 1. Seed Database (Create Admin & Default Rooms if needed)
            seedDatabase()

            // 2. Load Data
            refreshAllData()

            // 3. Check Onboarding Status from SharedPreferences
            val prefs = getApplication<Application>().getSharedPreferences("skale_prefs", Context.MODE_PRIVATE)
            val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
            _showOnboarding.value = !hasSeenOnboarding

            // 4. Simulate Splash Screen Delay
            delay(2500)
            _isLoading.value = false
        }
    }

    // --- ONBOARDING ACTION ---
    fun completeOnboarding() {
        val prefs = getApplication<Application>().getSharedPreferences("skale_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("has_seen_onboarding", true).apply()
        _showOnboarding.value = false
    }

    // --- DATA REFRESH ---
    private fun refreshAllData() {
        viewModelScope.launch {
            _rooms.value = db.dao().getAllRooms()
            if (_currentUser.value?.role == "admin") {
                loadAdminData()
            } else if (_currentUser.value != null) {
                _myBookings.value = db.dao().getUserBookings(_currentUser.value!!.email)
            }
        }
    }

    // --- FAVORITES ACTION ---
    fun toggleFavorite(room: RoomEntity) {
        viewModelScope.launch {
            val newStatus = !room.isFavorite
            db.dao().toggleFavorite(room.id, newStatus)
            refreshAllData()
        }
    }

    // --- BOOKING ACTION (With Conflict Detection) ---
    fun bookRoom(room: RoomEntity, date: String, timeSlot: String, cost: Double, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            // 1. Conflict Check
            val allBookings = db.dao().getAllBookings()
            val conflict = allBookings.any {
                it.roomId == room.id &&
                        it.date == date &&
                        it.displayTime == timeSlot &&
                        it.status != "rejected"
            }

            if (conflict) {
                onError("Slot unavailable! Room is already booked.")
                return@launch
            }

            // 2. Proceed with Booking
            val user = _currentUser.value ?: return@launch
            val booking = BookingEntity(
                userEmail = user.email, userName = user.name,
                roomName = room.name, roomAddress = room.loc, roomImage = room.img,
                date = date, displayTime = timeSlot, cost = cost, roomId = room.id,
                startTime = System.currentTimeMillis(), endTime = System.currentTimeMillis() + 3600000,
                status = "pending" // Default to pending approval
            )
            db.dao().addBooking(booking)
            _myBookings.value = db.dao().getUserBookings(user.email)
            onSuccess()
        }
    }

    // --- ADMIN ACTIONS ---
    fun addRoom(name: String, loc: String, cap: Int, img: String, price: Double) {
        viewModelScope.launch {
            val newRoom = RoomEntity(
                name = name, loc = loc, cap = cap, img = img, pricePerHour = price,
                amenities = listOf("Wifi", "TV", "AC", "Whiteboard"),
                rating = 5.0f, description = "Premium workspace added by Admin."
            )
            db.dao().insertRoom(newRoom)
            refreshAllData()
        }
    }

    fun deleteRoom(room: RoomEntity) {
        viewModelScope.launch {
            db.dao().deleteRoom(room)
            refreshAllData()
        }
    }

    fun updateBookingStatus(booking: BookingEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = booking.copy(status = newStatus)
            db.dao().updateBooking(updated)
            loadAdminData() // Refresh admin list
        }
    }

    private suspend fun loadAdminData() {
        _allBookings.value = db.dao().getAllBookings()
        val revenue = db.dao().getMonthlyRevenue() ?: 0.0
        val users = db.dao().getActiveUsersCount()

        _dashboardStats.value = DashboardStats(
            totalRooms = _rooms.value.size,
            activeBookings = _allBookings.value.count { it.status == "confirmed" },
            monthlyRevenue = revenue,
            activeUsers = users
        )

        // Weekly Stats Logic
        val dbStats = db.dao().getWeeklyBookingStats()
        val dayMap = mapOf("0" to "Sun", "1" to "Mon", "2" to "Tue", "3" to "Wed", "4" to "Thu", "5" to "Fri", "6" to "Sat")
        val fullWeek = dayMap.keys.map { key ->
            val count = dbStats.find { it.dayOfWeek == key }?.count ?: 0
            WeekDayStat(dayMap[key]!!, count)
        }
        _weeklyStats.value = fullWeek
    }

    // --- AUTHENTICATION ---
    fun login(email: String, pass: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val user = db.dao().login(email, pass)
            if (user != null) {
                _currentUser.value = user
                refreshAllData()
                onResult("Welcome ${user.name}!")
            } else {
                onResult("Invalid credentials")
            }
        }
    }

    fun signup(name: String, email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            db.dao().registerUser(UserEntity(name = name, email = email, pass = pass, role = "user"))
            onSuccess()
        }
    }

    fun logout() {
        _currentUser.value = null
        _myBookings.value = emptyList()
    }
    fun cancelBooking(booking: BookingEntity) {
        viewModelScope.launch {
            db.dao().cancelBooking(booking.id)
            // Refresh user list
            if (_currentUser.value != null) {
                _myBookings.value = db.dao().getUserBookings(_currentUser.value!!.email)
            }
            // Refresh admin list too (in case we are testing)
            _allBookings.value = db.dao().getAllBookings()
        }
    }

    // --- DATABASE SEEDING (Specific Chennai Data) ---
    private suspend fun seedDatabase() {
        // Create Default Admin
        if (db.dao().getAdmin() == null) {
            db.dao().registerUser(UserEntity(name = "Admin", email = "admin@skalelit.com", pass = "admin123", role = "admin"))
        }

        // Wipe old data if it doesn't match our Chennai scheme
        val currentRooms = db.dao().getAllRooms()
        val needsReset = currentRooms.any { it.name.contains("Galaxy") || it.name.contains("Nebula") }

        if (currentRooms.isEmpty() || needsReset) {
            currentRooms.forEach { db.dao().deleteRoom(it) }

            val newRooms = listOf(
                RoomEntity(
                    name = "Karuna Conclave",
                    loc = "AD 42 & 45, 4th Ave, Shanthi Colony, Anna Nagar, Chennai - 600040",
                    cap = 8, img = "https://www.skalelit.com/media/media/Karuna_Conclave_MR.jpeg",
                    pricePerHour = 800.0, amenities = listOf("Wifi", "TV", "Projector", "AC"), rating = 4.8f, description = "Premium 8-seater meeting room in the heart of Shanthi Colony."
                ),
                RoomEntity(
                    name = "F Block Suite",
                    loc = "3rd Floor, Plot No:264, Door No: F/22, F Block, Anna Nagar - 600102",
                    cap = 6, img = "https://www.skalelit.com/media/media/F_Block_MR__11zon.jpg",
                    pricePerHour = 600.0, amenities = listOf("Wifi", "Whiteboard", "AC"), rating = 4.5f, description = "Compact and quiet suite, perfect for small team huddles."
                ),
                RoomEntity(
                    name = "Celebrity Building",
                    loc = "2nd Floor, No. 49, 1st Street, 3rd Ave, Anna Nagar - 600040",
                    cap = 8, img = "https://www.skalelit.com/media/media/Celebrity_Building_MR.jpeg",
                    pricePerHour = 850.0, amenities = listOf("Wifi", "Video Conf", "Lounge", "Coffee"), rating = 4.9f, description = "Luxury meeting space with video conferencing facilities."
                ),
                RoomEntity(
                    name = "Sabari Towers",
                    loc = "W-110, Sabari Towers, 3rd Ave, Anna Nagar, Chennai - 600040",
                    cap = 8, img = "https://www.skalelit.com/media/media/Gokulam_Building_MR.jpeg",
                    pricePerHour = 750.0, amenities = listOf("Wifi", "Projector", "AC"), rating = 4.6f, description = "Professional corporate environment in Sabari Towers."
                ),
                RoomEntity(
                    name = "Korattur Hub",
                    loc = "249, 1B, 2nd St, Periyar Nagar, Korattur - 600080",
                    cap = 8, img = "https://www.skalelit.com/media/media/korattur-meeting-room_11zon.jpeg",
                    pricePerHour = 500.0, amenities = listOf("Wifi", "Parking", "AC"), rating = 4.4f, description = "Affordable and accessible hub in Korattur with ample parking."
                )
            )
            newRooms.forEach { db.dao().insertRoom(it) }
            _rooms.value = newRooms
        }
    }
}

class VMFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(app) as T
}
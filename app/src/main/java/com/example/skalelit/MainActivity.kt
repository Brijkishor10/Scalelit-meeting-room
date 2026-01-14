package com.example.skalelit

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.speech.RecognizerIntent
import android.view.HapticFeedbackConstants
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.room.*
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.math.cos
import kotlin.math.sin

val SurfaceWhite = Color(0xFFFFFFFF)
val BackgroundUltra = Color(0xFFF8FAFC)
val PrimaryIndigo = Color(0xFF4F46E5)
val SecondaryViolet = Color(0xFF7C3AED)
val TextHead = Color(0xFF111827)
val TextBody = Color(0xFF6B7280)
val SuccessTeal = Color(0xFF10B981)
val ErrorPink = Color(0xFFEF4444)
val InputFill = Color(0xFFF3F4F6)
val DividerColor = Color(0xFFE5E7EB)

val BrandGradient = Brush.horizontalGradient(listOf(PrimaryIndigo, SecondaryViolet))
val CardGradient = Brush.verticalGradient(listOf(Color.White, Color(0xFFFAFAFA)))

@Entity(tableName = "users") data class UserEntity(@PrimaryKey(autoGenerate = true) val id: Int = 0, val name: String, val email: String, val password: String)
@Entity(tableName = "bookings") data class BookingEntity(@PrimaryKey(autoGenerate = true) val id: Int = 0, val userEmail: String, val roomName: String, val roomAddress: String, val roomImage: String, val date: String, val startTime: Long, val endTime: Long, val displayTime: String, val cost: Double = 0.0, val roomId: String = "")
@Entity(tableName = "favorites") data class FavoriteEntity(@PrimaryKey(autoGenerate = true) val id: Int = 0, val userEmail: String, val roomId: String)
@Entity(tableName = "rooms") data class RoomEntity(@PrimaryKey(autoGenerate = true) val id: Int = 0, val name: String, val address: String, val capacity: Int, val imageUrl: String)
@Entity(tableName = "reviews") data class ReviewEntity(@PrimaryKey(autoGenerate = true) val id: Int = 0, val roomId: String, val userEmail: String, val rating: Int)

@Dao interface SkaleitDao {
    @Insert suspend fun registerUser(user: UserEntity)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1") suspend fun checkEmail(email: String): UserEntity?
    @Query("SELECT * FROM users WHERE email = :email AND password = :pass") suspend fun login(email: String, pass: String): UserEntity?
    @Insert suspend fun addBooking(booking: BookingEntity)
    @Delete suspend fun deleteBooking(booking: BookingEntity)
    @Query("SELECT * FROM bookings WHERE userEmail = :email") suspend fun getUserBookings(email: String): List<BookingEntity>
    @Insert suspend fun addFavorite(fav: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE userEmail = :email AND roomId = :roomId") suspend fun removeFavoriteById(email: String, roomId: String)
    @Query("SELECT * FROM favorites WHERE userEmail = :email") suspend fun getUserFavorites(email: String): List<FavoriteEntity>
    @Insert suspend fun addRoom(room: RoomEntity)
    @Query("SELECT * FROM rooms") suspend fun getAllRooms(): List<RoomEntity>
    @Insert suspend fun addReview(review: ReviewEntity)
    @Query("SELECT * FROM reviews") suspend fun getAllReviews(): List<ReviewEntity>
    @Query("SELECT COUNT(*) FROM bookings") suspend fun getBookingCount(): Int
    @Query("SELECT COUNT(*) FROM users") suspend fun getUserCount(): Int
    @Query("SELECT * FROM bookings") suspend fun getAllBookings(): List<BookingEntity>
}

@Database(entities = [UserEntity::class, BookingEntity::class, FavoriteEntity::class, RoomEntity::class, ReviewEntity::class], version = 6)
abstract class SkaleitDatabase : RoomDatabase() {
    abstract fun dao(): SkaleitDao
    companion object {
        @Volatile private var INSTANCE: SkaleitDatabase? = null
        fun getDatabase(context: Context): SkaleitDatabase { return INSTANCE ?: synchronized(this) { Room.databaseBuilder(context.applicationContext, SkaleitDatabase::class.java, "skaleit_db").fallbackToDestructiveMigration().build().also { INSTANCE = it } } }
    }
}

object SecurityUtils { fun hashPassword(input: String): String { val bytes = input.toByteArray(); val md = MessageDigest.getInstance("SHA-256"); return md.digest(bytes).fold("") { str, it -> str + "%02x".format(it) } } }
object PaymentSecurity { fun validateCard(number: String): Boolean { val digits = number.filter { it.isDigit() }.reversed().map { it.toString().toInt() }; if (digits.size < 13) return false; var sum = 0; for (i in digits.indices) { var n = digits[i]; if (i % 2 == 1) { n *= 2; if (n > 9) n -= 9 }; sum += n }; return sum % 10 == 0 } }
class NotificationHelper(private val context: Context) {
    private val CHANNEL_ID = "booking_channel"
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Bookings", NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    fun sendBookingNotification(roomName: String, time: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Booking Confirmed").setContentText("You booked $roomName for $time").setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true)
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
object BiometricHelper { fun showPrompt(activity: FragmentActivity, onSuccess: () -> Unit) { val executor: Executor = ContextCompat.getMainExecutor(activity); val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Authentication").setSubtitle("Verify identity to proceed").setNegativeButtonText("Cancel").build(); BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() { override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { super.onAuthenticationSucceeded(result); onSuccess() } }).authenticate(promptInfo) } }

data class MeetingRoomUI(val id: String, val name: String, val address: String, val imageUrl: String, val capacity: Int, var isBooked: Boolean = false, var isFavorite: Boolean = false, var averageRating: Float = 0f, var reviewCount: Int = 0)
data class DashboardStat(val label: String, val value: String, val icon: ImageVector, val color: Color)

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = SkaleitDatabase.getDatabase(this); val notificationHelper = NotificationHelper(this)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = PrimaryIndigo, secondary = TextBody, background = BackgroundUltra, surface = SurfaceWhite, onPrimary = SurfaceWhite, onBackground = TextHead, onSurface = TextHead)) {
                AppNavigation(db, notificationHelper)
            }
        }
    }
}

data class Confetti(val x: Float, val y: Float, val color: Color, val angle: Float)
@Composable
fun ConfettiExplosion() {
    val confettis = remember { List(50) { Confetti(x = 500f, y = 500f, color = listOf(PrimaryIndigo, SecondaryViolet, SuccessTeal, Color.Yellow).random(), angle = (0..360).random().toFloat()) } }
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) { anim.animateTo(1f, animationSpec = tween(1500, easing = LinearOutSlowInEasing)) }
    if (anim.value < 1f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            confettis.forEach { c ->
                val dist = anim.value * 1000f; val cx = center.x + cos(Math.toRadians(c.angle.toDouble())).toFloat() * dist; val cy = center.y + sin(Math.toRadians(c.angle.toDouble())).toFloat() * dist
                drawCircle(color = c.color.copy(alpha = 1f - anim.value), radius = 10f, center = Offset(cx, cy))
            }
        }
    }
}

@Composable
fun AppNavigation(db: SkaleitDatabase, notifHelper: NotificationHelper) {
    var currentScreen by remember { mutableStateOf("Login") }
    var currentUser by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(Unit) {
        if (db.dao().getAllRooms().isEmpty()) {
            db.dao().addRoom(RoomEntity(name = "Karuna Conclave", address = "Anna Nagar", capacity = 8, imageUrl = "https://www.skalelit.com/media/media/Karuna_Conclave_MR.jpeg"))
            db.dao().addRoom(RoomEntity(name = "F Block Suite", address = "Anna Nagar", capacity = 6, imageUrl = "https://www.skalelit.com/media/media/F_Block_MR__11zon.jpg"))
            db.dao().addRoom(RoomEntity(name = "Celebrity Building", address = "Anna Nagar", capacity = 8, imageUrl = "https://www.skalelit.com/media/media/Celebrity_Building_MR.jpeg"))
            db.dao().addRoom(RoomEntity(name = "Sabari Towers", address = "Anna Nagar", capacity = 8, imageUrl = "https://www.skalelit.com/media/media/Gokulam_Building_MR.jpeg"))
            db.dao().addRoom(RoomEntity(name = "Korattur Hub", address = "Korattur", capacity = 8, imageUrl = "https://www.skalelit.com/media/media/korattur-meeting-room_11zon.jpeg"))
        }
    }

    Crossfade(targetState = currentScreen, label = "Nav") { screen ->
        when (screen) {
            "Login" -> LoginScreen(db, { user -> currentUser = user; currentScreen = "Home" }, { currentScreen = "SignUp" }, { currentUser = UserEntity(0,"Demo User","user@skalelit.com",""); currentScreen = "Home" })
            "SignUp" -> SignUpScreen(db, { currentScreen = "Login" }, { currentScreen = "Login" })
            "Home" -> if (currentUser != null) MainAppStructure(db, notifHelper, currentUser!!) { currentUser = null; currentScreen = "Login" }
            "AddRoom" -> AddRoomScreen(db) { currentScreen = "Home" }
        }
    }
}

@Composable
fun SleekTextField(value: String, onValueChange: (String) -> Unit, label: String, isPassword: Boolean = false, keyboardType: KeyboardType = KeyboardType.Text) {
    Column {
        Text(label, color = TextHead, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
        BasicTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(InputFill).padding(horizontal = 16.dp),
            textStyle = TextStyle(color = TextHead, fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            singleLine = true,
            decorationBox = { innerTextField -> Box(contentAlignment = Alignment.CenterStart) { if (value.isEmpty()) Text("Enter $label...", color = TextBody.copy(alpha = 0.5f)); innerTextField() } }
        )
    }
}

@Composable
fun SleekButton(text: String, onClick: () -> Unit, color: Color = PrimaryIndigo, gradient: Boolean = true) {
    val view = LocalView.current
    Box(
        modifier = Modifier.fillMaxWidth().height(56.dp).shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = color.copy(alpha=0.4f)).clip(RoundedCornerShape(16.dp)).background(if (gradient) BrandGradient else androidx.compose.ui.graphics.SolidColor(color)).clickable { view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS); onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SurfaceWhite)
    }
}

@Composable
fun SleekCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val view = LocalView.current
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(2.dp), modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp).let { if(onClick!=null) it.clickable { view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK); onClick() } else it }) {
        Column(modifier = Modifier.padding(0.dp)) { content() }
    }
}

@Composable
fun SelectionPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val view = LocalView.current
    Box(
        modifier = Modifier.clip(RoundedCornerShape(50)).background(if (selected) PrimaryIndigo else SurfaceWhite)
            .border(1.dp, if (selected) PrimaryIndigo else DividerColor, RoundedCornerShape(50))
            .clickable { view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK); onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (selected) SurfaceWhite else TextHead)
    }
}

@Composable
fun LoginScreen(db: SkaleitDatabase, onLoginSuccess: (UserEntity) -> Unit, onNavigateToSignUp: () -> Unit, onBiometricSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }; val scope = rememberCoroutineScope(); val context = LocalContext.current; val activity = context as? FragmentActivity
    Box(modifier = Modifier.fillMaxSize().background(BackgroundUltra)) {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            AsyncImage(model = "https://skalelit.com/assets/Skalelit__Logo_1_png-CDnnzTmQ.png", contentDescription = null, modifier = Modifier.width(180.dp).height(70.dp), contentScale = ContentScale.Fit)
            Spacer(modifier = Modifier.height(48.dp))
            Text("Welcome Back", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextHead)
            Text("Sign in to your workspace.", fontSize = 16.sp, color = TextBody)
            Spacer(modifier = Modifier.height(40.dp))
            SleekTextField(value = email, onValueChange = { email = it }, label = "Email")
            Spacer(modifier = Modifier.height(20.dp))
            SleekTextField(value = password, onValueChange = { password = it }, label = "Password", isPassword = true)
            Spacer(modifier = Modifier.height(40.dp))
            SleekButton(text = "Sign In", onClick = { scope.launch { val user = db.dao().login(email, SecurityUtils.hashPassword(password)); if (user != null) onLoginSuccess(user) else Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_SHORT).show() } })
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(onClick = { activity?.let { BiometricHelper.showPrompt(it, onBiometricSuccess) } }, modifier = Modifier.size(64.dp).background(SurfaceWhite, CircleShape).border(1.dp, DividerColor, CircleShape)) { Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(32.dp)) }
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onNavigateToSignUp) { Text("Create Account", color = TextBody, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
fun SignUpScreen(db: SkaleitDatabase, onSignUpSuccess: () -> Unit, onNavigateToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }; val scope = rememberCoroutineScope(); val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().background(BackgroundUltra).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Create Account", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextHead); Spacer(modifier = Modifier.height(32.dp))
        SleekTextField(value = name, onValueChange = { name = it }, label = "Full Name"); Spacer(modifier = Modifier.height(20.dp))
        SleekTextField(value = email, onValueChange = { email = it }, label = "Email"); Spacer(modifier = Modifier.height(20.dp))
        SleekTextField(value = password, onValueChange = { password = it }, label = "Password", isPassword = true); Spacer(modifier = Modifier.height(40.dp))
        SleekButton(text = "Sign Up", onClick = { scope.launch { if (db.dao().checkEmail(email) == null) { db.dao().registerUser(UserEntity(name = name, email = email, password = SecurityUtils.hashPassword(password))); Toast.makeText(context, "Welcome!", Toast.LENGTH_SHORT).show(); onSignUpSuccess() } else Toast.makeText(context, "Email taken", Toast.LENGTH_SHORT).show() } })
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToLogin) { Text("Back to Login", color = TextBody) }
    }
}

@Composable
fun AddRoomScreen(db: SkaleitDatabase, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }; var address by remember { mutableStateOf("") }; var capacity by remember { mutableStateOf("") }; var imageUrl by remember { mutableStateOf("https://www.skalelit.com/media/media/Gokulam_Building_MR.jpeg") }; val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize().background(BackgroundUltra).padding(24.dp)) {
        Spacer(modifier = Modifier.height(40.dp)); Text("New Room", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextHead); Spacer(modifier = Modifier.height(24.dp))
        SleekTextField(value = name, onValueChange = { name = it }, label = "Room Name"); Spacer(modifier = Modifier.height(16.dp))
        SleekTextField(value = address, onValueChange = { address = it }, label = "Address"); Spacer(modifier = Modifier.height(16.dp))
        SleekTextField(value = capacity, onValueChange = { capacity = it }, label = "Capacity", keyboardType = KeyboardType.Number); Spacer(modifier = Modifier.height(16.dp))
        SleekTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = "Image URL"); Spacer(modifier = Modifier.height(32.dp))
        SleekButton(text = "Save Room", onClick = { scope.launch { db.dao().addRoom(RoomEntity(name = name, address = address, capacity = capacity.toIntOrNull() ?: 5, imageUrl = imageUrl)); onBack() } }, color = Green)
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancel", color = Red) }
    }
}

@Composable
fun MainAppStructure(db: SkaleitDatabase, notifHelper: NotificationHelper, currentUser: UserEntity, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }; var currentSubScreen by remember { mutableStateOf<String?>(null) }
    var selectedRoom by remember { mutableStateOf<MeetingRoomUI?>(null) }; var selectedBooking by remember { mutableStateOf<BookingEntity?>(null) }
    var allRooms by remember { mutableStateOf<List<MeetingRoomUI>>(emptyList()) }; var myBookings by remember { mutableStateOf<List<BookingEntity>>(emptyList()) }
    var stats by remember { mutableStateOf(listOf<DashboardStat>()) }; var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }; val scope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var showSuccessDialog by remember { mutableStateOf(false) }

    fun refreshData() {
        scope.launch {
            val roomEntities = db.dao().getAllRooms(); val dbBookings = db.dao().getUserBookings(currentUser.email); val dbFavorites = db.dao().getUserFavorites(currentUser.email); val allReviews = db.dao().getAllReviews(); val allGlobalBookings = db.dao().getAllBookings()
            val now = System.currentTimeMillis()

            if (currentUser.email == "admin@skalelit.com") {
                val totalBookings = db.dao().getBookingCount(); val totalUsers = db.dao().getUserCount(); val allB = db.dao().getAllBookings(); val revenue = allB.sumOf { it.cost }
                stats = listOf(DashboardStat("Revenue", "$${revenue.toInt()}", Icons.Default.AttachMoney, Green), DashboardStat("Bookings", "$totalBookings", Icons.Default.DateRange, PrimaryIndigo), DashboardStat("Users", "$totalUsers", Icons.Default.Person, Color(0xFFF59E0B)), DashboardStat("Rooms", "${roomEntities.size}", Icons.Default.Domain, Color(0xFFEC4899)))
            }

            allRooms = roomEntities.map { entity ->
                val roomReviews = allReviews.filter { it.roomId == entity.id.toString() }; val avgRating = if (roomReviews.isNotEmpty()) roomReviews.map { it.rating }.average().toFloat() else 0f
                val isCurrentlyOccupied = allGlobalBookings.any { b -> b.roomId == entity.id.toString() && now >= b.startTime && now <= b.endTime }
                MeetingRoomUI(id = entity.id.toString(), name = entity.name, address = entity.address, imageUrl = entity.imageUrl, capacity = entity.capacity, isFavorite = dbFavorites.any { it.roomId == entity.id.toString() }, averageRating = avgRating, reviewCount = roomReviews.size, isBooked = isCurrentlyOccupied)
            }
            myBookings = dbBookings; isLoading = false
        }
    }

    LaunchedEffect(Unit) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS); refreshData() }
    val isAdmin = currentUser.email == "admin@skalelit.com"

    if (showSuccessDialog) {
        ConfettiExplosion()
        AlertDialog(onDismissRequest = { showSuccessDialog = false }, containerColor = SurfaceWhite, icon = { Icon(Icons.Default.CheckCircle, null, tint = Green, modifier = Modifier.size(48.dp)) }, title = { Text("Booking Confirmed!", fontWeight = FontWeight.Bold) }, text = { Text("Your space has been reserved successfully.", color = TextBody) }, confirmButton = { Button(onClick = { showSuccessDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)) { Text("Awesome") } })
    }

    if (currentSubScreen == "AddRoom") { AddRoomScreen(db) { currentSubScreen = null; refreshData() } }
    else {
        Scaffold(bottomBar = { if (selectedRoom == null && selectedBooking == null) BottomNavBar(selectedTab) { selectedTab = it } }) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(BackgroundUltra)) {
                BackHandler(enabled = selectedRoom != null || selectedBooking != null) { selectedRoom = null; selectedBooking = null }
                if (selectedRoom != null) {
                    PremiumDetailScreen(selectedRoom!!, { selectedRoom = null }) { date, startMillis, durationHours, cost ->
                        scope.launch {
                            val bookedRoom = selectedRoom!!; val endMillis = startMillis + (durationHours * 3600000)
                            if (startMillis < System.currentTimeMillis()) { snackbarHostState.showSnackbar("⛔ Cannot book in the past!"); return@launch }

                            val allB = db.dao().getAllBookings()
                            val conflict = allB.any { it.roomId == bookedRoom.id && it.date == date && (startMillis < it.endTime && endMillis > it.startTime) }

                            if (conflict) { snackbarHostState.showSnackbar("⚠️ Room is busy at this time.") }
                            else {
                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()); val displayStr = "${timeFormat.format(startMillis)} - ${timeFormat.format(endMillis)}"
                                val newBooking = BookingEntity(userEmail = currentUser.email, roomName = bookedRoom.name, roomAddress = bookedRoom.address, roomImage = bookedRoom.imageUrl, date = date, startTime = startMillis, endTime = endMillis, displayTime = displayStr, cost = cost, roomId = bookedRoom.id)
                                db.dao().addBooking(newBooking); notifHelper.sendBookingNotification(bookedRoom.name, displayStr); showSuccessDialog = true; refreshData(); selectedRoom = null; selectedTab = 1
                            }
                        }
                    }
                } else if (selectedBooking != null) {
                    PremiumTicketScreen(selectedBooking!!, { selectedBooking = null }) { scope.launch { db.dao().deleteBooking(selectedBooking!!); refreshData(); selectedBooking = null; snackbarHostState.showSnackbar("Booking Cancelled") } }
                } else {
                    when (selectedTab) {
                        0 -> if (isAdmin) AdminDashboardScreen(stats, allRooms) { currentSubScreen = "AddRoom" } else PremiumHomeScreen(allRooms, isLoading, currentUser.name, isAdmin, { selectedRoom = it }, { room -> scope.launch { if (room.isFavorite) { db.dao().removeFavoriteById(currentUser.email, room.id); snackbarHostState.showSnackbar("Removed") } else { db.dao().addFavorite(FavoriteEntity(userEmail = currentUser.email, roomId = room.id)); snackbarHostState.showSnackbar("Saved") }; refreshData() } }, { currentSubScreen = "AddRoom" })
                        1 -> PremiumBookingsScreen(myBookings, { selectedBooking = it }, { booking, rating -> scope.launch { db.dao().addReview(ReviewEntity(roomId = booking.roomId, userEmail = currentUser.email, rating = rating)); refreshData(); snackbarHostState.showSnackbar("Rated") } })
                        2 -> PremiumProfileScreen(currentUser, onLogout)
                    }
                }
                SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(stats: List<DashboardStat>, rooms: List<MeetingRoomUI>, onAddRoom: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Dashboard", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextHead); Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) { if (stats.isNotEmpty()) { AdminStatCard(stats[0], Modifier.weight(1f)); AdminStatCard(stats[1], Modifier.weight(1f)) } }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) { if (stats.size >= 4) { AdminStatCard(stats[2], Modifier.weight(1f)); AdminStatCard(stats[3], Modifier.weight(1f)) } }
            }
        }
        item { Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Manage Rooms", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextHead); IconButton(onClick = onAddRoom) { Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(32.dp)) } }; Spacer(modifier = Modifier.height(16.dp)) }
        items(rooms) { room -> AdminRoomItem(room); Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun AdminStatCard(stat: DashboardStat, modifier: Modifier = Modifier) {
    var currentValue by remember { mutableIntStateOf(0) }
    val targetValue = stat.value.replace("$", "").toIntOrNull() ?: 0
    LaunchedEffect(key1 = targetValue) { animate(0f, targetValue.toFloat(), animationSpec = tween(1000)) { value, _ -> currentValue = value.toInt() } }
    SleekCard(modifier = modifier.height(120.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(stat.color.copy(alpha=0.1f)), contentAlignment = Alignment.Center) { Icon(imageVector = stat.icon, contentDescription = null, tint = stat.color, modifier = Modifier.size(20.dp)) }
            Column { Text(if(stat.value.contains("$")) "$$currentValue" else "$currentValue", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextHead); Text(stat.label, fontSize = 12.sp, color = TextBody) }
        }
    }
}

@Composable
fun AdminRoomItem(room: MeetingRoomUI) {
    SleekCard {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = room.imageUrl, null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { Text(room.name, fontWeight = FontWeight.SemiBold, color = TextHead); Text("Capacity: ${room.capacity}", fontSize = 13.sp, color = TextBody) }
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = TextBody)
        }
    }
}

@Composable
fun PremiumHomeScreen(rooms: List<MeetingRoomUI>, isLoading: Boolean, userName: String, isAdmin: Boolean, onRoomClick: (MeetingRoomUI) -> Unit, onToggleFavorite: (MeetingRoomUI) -> Unit, onAdminAddClick: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }; val filters = listOf("All", "Favorites", "Meeting", "Event")
    var selectedFilter by remember { mutableStateOf("All") }
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> val data = result.data; val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0); if (spokenText != null) searchQuery = spokenText }
    val displayedRooms = remember(rooms, selectedFilter, searchQuery) { rooms.filter { room -> (when(selectedFilter) { "All" -> true; "Favorites" -> room.isFavorite; "Meeting" -> room.capacity in 3..10; "Event" -> room.capacity > 10; else -> true }) && room.name.contains(searchQuery, ignoreCase = true) } }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(model = "https://skalelit.com/assets/Skalelit__Logo_1_png-CDnnzTmQ.png", contentDescription = null, modifier = Modifier.width(140.dp).height(50.dp), contentScale = ContentScale.Fit)
                    IconButton(onClick = {}, modifier = Modifier.size(44.dp).clip(CircleShape).background(SurfaceWhite).shadow(elevation = 2.dp, shape = CircleShape)) { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = TextHead) }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text("Hello, $userName", fontSize = 16.sp, color = TextBody); Text("Find your perfect\nworkspace.", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = TextHead, lineHeight = 40.sp)
                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth().height(60.dp).shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(SurfaceWhite).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextBody)
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.weight(1f), textStyle = TextStyle(fontSize = 16.sp, color = TextHead), singleLine = true, decorationBox = { innerTextField -> if (searchQuery.isEmpty()) Text("Search rooms...", color = TextBody.copy(alpha=0.5f), fontSize = 16.sp) ; innerTextField() })
                    IconButton(onClick = { val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM) }; try { voiceLauncher.launch(intent) } catch(e:Exception) {} }) { Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = PrimaryIndigo) }
                }
            }
        }
        item { LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom=24.dp)) { items(filters) { filter -> SelectionPill(text = filter, selected = selectedFilter == filter) { selectedFilter = filter } } } }
        if (isLoading) item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryIndigo) } }
        else if (displayedRooms.isEmpty()) { item { Box(modifier = Modifier.fillMaxWidth().padding(top=40.dp), contentAlignment = Alignment.Center) { Text("No rooms found.", color = TextBody) } } }
        else { itemsIndexed(displayedRooms) { index, room ->
            val animatedAlpha = remember { Animatable(0f) }
            LaunchedEffect(Unit) { delay(index * 100L); animatedAlpha.animateTo(1f) }
            Box(modifier = Modifier.alpha(animatedAlpha.value)) { ClassyRoomCard(room, onRoomClick, onToggleFavorite) }
            Spacer(modifier = Modifier.height(24.dp))
        } }
    }
    if (isAdmin) FloatingActionButton(onClick = onAdminAddClick, containerColor = PrimaryIndigo, modifier = Modifier.padding(bottom = 100.dp, end = 24.dp)) { Icon(imageVector = Icons.Default.Add, contentDescription = "Add Room", tint = SurfaceWhite) }
}

@Composable
fun ClassyRoomCard(room: MeetingRoomUI, onClick: (MeetingRoomUI) -> Unit, onToggleFavorite: (MeetingRoomUI) -> Unit) {
    SleekCard(onClick = { onClick(room) }) {
        Column {
            Box(modifier = Modifier.height(220.dp).fillMaxWidth()) {
                AsyncImage(model = room.imageUrl, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha=0.7f)))))
                IconButton(onClick = { onToggleFavorite(room) }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(SurfaceWhite.copy(alpha=0.9f), CircleShape).size(40.dp)) { Icon(imageVector = if(room.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if(room.isFavorite) Red else TextBody, modifier = Modifier.size(20.dp)) }
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Text(room.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SurfaceWhite)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null, tint = SurfaceWhite.copy(alpha=0.8f), modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text(room.address, fontSize = 14.sp, color = SurfaceWhite.copy(alpha=0.9f)) }
                }
                Box(modifier = Modifier.align(Alignment.TopStart).padding(16.dp).clip(RoundedCornerShape(50)).background(SurfaceWhite.copy(alpha=0.95f)).padding(horizontal = 12.dp, vertical = 6.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if(room.isBooked) Red else Green)); Spacer(modifier = Modifier.width(6.dp)); Text(if(room.isBooked) "Busy" else "Available", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextHead) } }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumDetailScreen(room: MeetingRoomUI, onBack: () -> Unit, onBookConfirm: (String, Long, Int, Double) -> Unit) {
    val context = LocalContext.current; val calendar = Calendar.getInstance()
    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }; var selectedStartMillis by remember { mutableLongStateOf(0L) }
    var durationHours by remember { mutableFloatStateOf(1f) }
    var addCoffee by remember { mutableStateOf(false) }; var addTech by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    fun showPickers() {
        val y = calendar.get(Calendar.YEAR); val m = calendar.get(Calendar.MONTH); val d = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(context, { _, year, month, day ->
            val h = calendar.get(Calendar.HOUR_OF_DAY)
            TimePickerDialog(context, { _, hour, _ ->
                val selectedCal = Calendar.getInstance(); selectedCal.set(year, month, day, hour, 0)
                selectedDate = "$day/${month + 1}/$year"; selectedStartMillis = selectedCal.timeInMillis; showTimeDialog = true
            }, h, 0, true).show()
        }, y, m, d).show()
    }

    val baseRate = 50.0
    val extras = (if(addCoffee) 15.0 else 0.0) + (if(addTech) 30.0 else 0.0)
    val estimatedTotal = (durationHours * baseRate) + extras

    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false }, containerColor = SurfaceWhite,
            title = { Text("Confirm Booking", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Date: $selectedDate", color = TextBody); Spacer(modifier = Modifier.height(16.dp))
                    Text("Duration: ${durationHours.toInt()} Hours", fontWeight = FontWeight.SemiBold)
                    Slider(value = durationHours, onValueChange = { durationHours = it }, valueRange = 1f..4f, steps = 2, colors = SliderDefaults.colors(thumbColor = PrimaryIndigo, activeTrackColor = PrimaryIndigo))
                    Divider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { Text("Amenities:", color = TextBody); Text("$${extras.toInt()}", fontWeight = FontWeight.Bold, color = TextHead) }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { Text("Total:", fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("$${estimatedTotal.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryIndigo) }
                }
            },
            confirmButton = { Button(onClick = { showTimeDialog = false; onBookConfirm(selectedDate, selectedStartMillis, durationHours.toInt(), estimatedTotal) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)) { Text("Book Now") } },
            dismissButton = { TextButton(onClick = { showTimeDialog = false }) { Text("Cancel") } }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundUltra)) {
        Column(modifier = Modifier.verticalScroll(scrollState).padding(bottom = 100.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(320.dp).graphicsLayer { translationY = scrollState.value * 0.5f }) {
                AsyncImage(model = room.imageUrl, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha=0.3f)))))
            }
            Column(modifier = Modifier.fillMaxSize().background(SurfaceWhite, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).offset(y = (-40).dp).padding(32.dp)) {
                Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(40.dp).height(4.dp).background(DividerColor, CircleShape)); Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column { Text(room.name, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextHead); Text(room.address, color = TextBody, fontSize = 14.sp) }
                    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(PrimaryIndigo.copy(0.1f)).padding(horizontal = 12.dp, vertical = 8.dp)) { Text("$${baseRate.toInt()}/hr", color = PrimaryIndigo, fontWeight = FontWeight.Bold) }
                }
                Spacer(modifier = Modifier.height(32.dp)); Text("Amenities", fontWeight = FontWeight.Bold, color = TextHead); Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SelectionPill(text = "Coffee +$15", selected = addCoffee) { addCoffee = !addCoffee }
                    SelectionPill(text = "Tech Pack +$30", selected = addTech) { addTech = !addTech }
                }
                Spacer(modifier = Modifier.height(32.dp)); Text("Details", fontWeight = FontWeight.Bold, color = TextHead); Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { DetailIcon(Icons.Default.Wifi, "Fast Wifi"); DetailIcon(Icons.Default.ConnectedTv, "Display"); DetailIcon(Icons.Default.Coffee, "Lounge"); DetailIcon(Icons.Default.Security, "Secure") }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, SurfaceWhite))).padding(24.dp)) {
            SleekButton(text = "Check Availability", onClick = { showPickers() })
        }
        IconButton(onClick = onBack, modifier = Modifier.padding(top = 48.dp, start = 24.dp).background(SurfaceWhite, CircleShape).shadow(4.dp, CircleShape)) { Icon(Icons.Default.ArrowBack, null, tint = TextHead) }
    }
}

@Composable
fun PremiumBookingsScreen(bookings: List<BookingEntity>, onBookingClick: (BookingEntity) -> Unit, onRateBooking: (BookingEntity, Int) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }; val now = System.currentTimeMillis(); val upcoming = bookings.filter { it.endTime > now }.sortedBy { it.startTime }; val history = bookings.filter { it.endTime <= now }.sortedByDescending { it.startTime }; val displayList = if (selectedTab == 0) upcoming else history
    var showRatingDialog by remember { mutableStateOf(false) }; var selectedBookingToRate by remember { mutableStateOf<BookingEntity?>(null) }

    if (showRatingDialog && selectedBookingToRate != null) {
        var currentRating by remember { mutableIntStateOf(5) }
        AlertDialog(onDismissRequest = { showRatingDialog = false }, containerColor = SurfaceWhite, title = { Text("Rate experience") }, text = { Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) { for (i in 1..5) { Icon(Icons.Default.Star, null, tint = if(i <= currentRating) Color(0xFFFFC107) else DividerColor, modifier = Modifier.size(32.dp).clickable { currentRating = i }) } } }, confirmButton = { Button(onClick = { onRateBooking(selectedBookingToRate!!, currentRating); showRatingDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)) { Text("Submit") } }, dismissButton = { TextButton(onClick = { showRatingDialog = false }) { Text("Cancel") } })
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(48.dp)); Text("My Bookings", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextHead); Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth().height(52.dp).background(InputFill, RoundedCornerShape(14.dp)).padding(4.dp)) { Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(10.dp)).background(if (selectedTab == 0) SurfaceWhite else Color.Transparent).clickable { selectedTab = 0 }.shadow(elevation = if(selectedTab==0) 2.dp else 0.dp, shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text("Upcoming", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 0) TextHead else TextBody) }; Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(10.dp)).background(if (selectedTab == 1) SurfaceWhite else Color.Transparent).clickable { selectedTab = 1 }.shadow(elevation = if(selectedTab==1) 2.dp else 0.dp, shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text("History", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 1) TextHead else TextBody) } }; Spacer(modifier = Modifier.height(24.dp))
        if (displayList.isEmpty()) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.EventNote, null, tint = DividerColor, modifier = Modifier.size(64.dp)); Spacer(modifier = Modifier.height(16.dp)); Text("No bookings yet", color = TextBody) } }
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) { items(displayList) { booking -> SleekCard(onClick = { onBookingClick(booking) }) { Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(BackgroundUltra, RoundedCornerShape(12.dp)).padding(12.dp)) { Text(booking.date.split("/")[0], fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextHead); Text("Day", fontSize = 12.sp, color = TextBody) } ; Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(booking.roomName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextHead); Text(booking.displayTime, fontSize = 14.sp, color = PrimaryIndigo, fontWeight = FontWeight.Medium); if(selectedTab == 1) TextButton(onClick = { selectedBookingToRate = booking; showRatingDialog = true }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) { Text("Rate Room", fontSize = 12.sp) } }; Icon(Icons.Default.ChevronRight, null, tint = TextBody) } } }; item { Spacer(modifier = Modifier.height(100.dp)) } }
    }
}

@Composable
fun PremiumTicketScreen(booking: BookingEntity, onBack: () -> Unit, onCancel: () -> Unit) {
    var showCancelDialog by remember { mutableStateOf(false) }; val context = LocalContext.current
    if (showCancelDialog) AlertDialog(onDismissRequest = { showCancelDialog = false }, title = { Text("Cancel Booking?") }, text = { Text("This action cannot be undone.") }, confirmButton = { Button(onClick = { showCancelDialog = false; onCancel() }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("Confirm Cancel") } }, dismissButton = { TextButton(onClick = { showCancelDialog = false }) { Text("Go Back") } })
    Box(modifier = Modifier.fillMaxSize().background(BackgroundUltra)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Spacer(modifier = Modifier.height(40.dp)); IconButton(onClick = onBack, modifier = Modifier.background(SurfaceWhite, CircleShape).shadow(2.dp, CircleShape)) { Icon(Icons.Default.Close, null, tint = TextHead) }
            Spacer(modifier = Modifier.height(24.dp)); Text("Digital Pass", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextHead); Spacer(modifier = Modifier.height(24.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(booking.roomName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextHead); Text("Authorized Access", color = Green, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(32.dp)); Divider(color = DividerColor, thickness = 1.dp); Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("DATE", fontSize = 12.sp, color = TextBody); Text(booking.date, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextHead) } ; Column(horizontalAlignment = Alignment.End) { Text("TIME", fontSize = 11.sp, color = TextBody); Text(booking.displayTime, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextHead) } }
                    Spacer(modifier = Modifier.height(40.dp)); Box(modifier = Modifier.size(180.dp).border(1.dp, TextHead, RoundedCornerShape(16.dp)).padding(12.dp)) { AsyncImage(model = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${booking.id}", contentDescription = "QR", modifier = Modifier.fillMaxSize()) }; Spacer(modifier = Modifier.height(16.dp)); Text("Scan at entry kiosk", fontSize = 14.sp, color = TextBody)
                }
            }
            Spacer(modifier = Modifier.weight(1f)); TextButton(onClick = { showCancelDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Cancel Booking", color = Red) }; Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PremiumProfileScreen(user: UserEntity, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(modifier = Modifier.height(48.dp)); Text("Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextHead); Spacer(modifier = Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(InputFill), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = TextBody, modifier = Modifier.size(40.dp)) }; Spacer(modifier = Modifier.width(24.dp)); Column { Text(user.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextHead); Text(user.email, color = TextBody) } }
        Spacer(modifier = Modifier.height(48.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { SleekCard(onClick = {}) { Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Settings, null, tint = TextHead); Spacer(modifier = Modifier.width(16.dp)); Text("Settings", color = TextHead, fontSize = 16.sp, fontWeight = FontWeight.Medium) } }; SleekCard(onClick = {}) { Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Help, null, tint = TextHead); Spacer(modifier = Modifier.width(16.dp)); Text("Help & Support", color = TextHead, fontSize = 16.sp, fontWeight = FontWeight.Medium) } } }
        Spacer(modifier = Modifier.weight(1f)); SleekButton(text = "Log Out", onClick = onLogout, color = SurfaceWhite); Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun BottomNavBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(90.dp)) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, SurfaceWhite.copy(alpha=0.95f)))))
        Row(modifier = Modifier.fillMaxSize().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            NavBarItem(Icons.Default.Home, "Home", selectedTab == 0) { onTabSelected(0) }
            NavBarItem(Icons.Default.DateRange, "Bookings", selectedTab == 1) { onTabSelected(1) }
            NavBarItem(Icons.Default.Person, "Profile", selectedTab == 2) { onTabSelected(2) }
        }
    }
}

@Composable
fun NavBarItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if(isSelected) PrimaryIndigo else TextBody
    val scale by animateFloatAsState(if(isSelected) 1.2f else 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }.scale(scale)) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = color, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun DetailIcon(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(InputFill), contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, tint = TextHead) }
        Spacer(modifier = Modifier.height(8.dp)); Text(label, fontSize = 12.sp, color = TextBody)
    }
}
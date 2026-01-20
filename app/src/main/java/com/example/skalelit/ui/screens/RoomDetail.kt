package com.example.skalelit.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.skalelit.data.entity.RoomEntity
import com.example.skalelit.ui.theme.SkalePrimary
import java.util.*

@Composable
fun RoomDetail(room: RoomEntity, onDismiss: () -> Unit, onBook: (String, String) -> Unit) {
    val ctx = LocalContext.current
    val cal = Calendar.getInstance()

    BackHandler { onDismiss() }

    fun pickDateAndTime() {
        val datePicker = DatePickerDialog(ctx, { _, y, m, d ->
            TimePickerDialog(ctx, { _, h, min -> onBook("${d}/${m + 1}/${y}", "${h}:${min}") },
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f))) {
        Box(Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() })

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxHeight(0.95f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
        ) {
            // HEADER IMAGE
            Box(Modifier.height(300.dp).fillMaxWidth()) {
                AsyncImage(
                    model = room.img, contentDescription = null,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                )
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(0.8f)), startY = 200f)))

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(16.dp).background(Color.White, CircleShape).size(40.dp)
                ) { Icon(Icons.Rounded.Close, null, tint = Color.Black) }

                Column(Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                    Text(room.name, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, null, tint = Color.White.copy(0.9f), modifier = Modifier.size(16.dp))
                        Text(" ${room.loc}", color = Color.White.copy(0.9f), fontSize = 14.sp)
                    }
                }
            }

            // CONTENT
            Column(Modifier.weight(1f).padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    RoomFeature(Icons.Rounded.Groups, "${room.cap} Seats")
                    RoomFeature(Icons.Rounded.Star, "${room.rating} Rating")
                    RoomFeature(Icons.Rounded.Wifi, "Free Wifi")
                }

                Spacer(Modifier.height(32.dp))
                Text("Description", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(Modifier.height(8.dp))
                Text(room.description, lineHeight = 24.sp, color = Color(0xFF64748B))

                Spacer(Modifier.height(32.dp))
                Text("Facilities", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(Modifier.height(12.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    room.amenities.forEach {
                        Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp)) {
                            Text(it, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                OutlinedButton(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(room.loc)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try { ctx.startActivity(mapIntent) } catch (e: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Map, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Get Directions")
                }
            }

            // BOTTOM BAR
            Surface(shadowElevation = 20.dp, color = Color.White) {
                Row(Modifier.padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Price", fontSize = 12.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$${room.pricePerHour}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SkalePrimary)
                            Text("/hr", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }
                    Button(
                        onClick = { pickDateAndTime() },
                        colors = ButtonDefaults.buttonColors(containerColor = SkalePrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(56.dp).width(180.dp)
                    ) {
                        Text("Book Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RoomFeature(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF8FAFC), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = SkalePrimary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
        }
    }
}
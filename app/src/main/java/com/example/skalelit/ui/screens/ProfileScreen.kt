package com.example.skalelit.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skalelit.data.entity.UserEntity
import com.example.skalelit.ui.components.SkaleButton
import com.example.skalelit.ui.theme.SkalePrimary
import com.example.skalelit.ui.theme.SkaleTextBody
import com.example.skalelit.ui.theme.SkaleTextHead

@Composable
fun ProfileScreen(user: UserEntity, onLogout: () -> Unit) {
    val ctx = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
    ) {
        // --- HEADER SECTION ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SkalePrimary)
                .padding(top = 40.dp, bottom = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkalePrimary
                    )
                }
                Spacer(Modifier.height(16.dp))
                // Name & Email
                Text(user.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(user.email, fontSize = 14.sp, color = Color.White.copy(0.7f))

                Spacer(Modifier.height(24.dp))

                // Edit Button Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(0.2f),
                    modifier = Modifier.clickable { /* TODO: Open Edit Dialog */ }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profile", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- STATS OVERVIEW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .offset(y = (-30).dp) // Overlap effect
        ) {
            ProfileStatBox("Membership", "Gold", Icons.Rounded.WorkspacePremium, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            ProfileStatBox("Bookings", "12", Icons.Rounded.History, Modifier.weight(1f))
        }

        // --- SETTINGS MENU ---
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text("General", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SkaleTextBody)
            Spacer(Modifier.height(12.dp))

            SettingsItem(Icons.Rounded.Person, "Account Information") {}
            SettingsItem(Icons.Rounded.Payment, "Payment Methods") {}
            SettingsItem(Icons.Rounded.Notifications, "Notifications") {}

            Spacer(Modifier.height(24.dp))
            Text("Support", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SkaleTextBody)
            Spacer(Modifier.height(12.dp))

            SettingsItem(Icons.Rounded.Help, "Help Center") {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://skalelit.com/support"))
                try { ctx.startActivity(intent) } catch (_: Exception) {}
            }
            SettingsItem(Icons.Rounded.PrivacyTip, "Privacy Policy") {}

            Spacer(Modifier.height(32.dp))

            SkaleButton("Log Out") { onLogout() }

            Spacer(Modifier.height(32.dp))
            Text("Version 1.0.0", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.LightGray, fontSize = 12.sp)
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileStatBox(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = SkalePrimary)
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SkaleTextHead)
            Text(label, fontSize = 12.sp, color = SkaleTextBody)
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SkalePrimary.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = SkalePrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp, color = SkaleTextHead, fontWeight = FontWeight.Medium)
            // FIX: Used standard ChevronRight instead of AutoMirrored
            Icon(Icons.Rounded.ChevronRight, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
package com.example.skalelit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import com.example.skalelit.ui.components.SkaleButton
import com.example.skalelit.ui.components.SkaleInput
import com.example.skalelit.ui.theme.SkalePrimary
import com.example.skalelit.ui.theme.SkaleSurface
import com.example.skalelit.ui.theme.SkaleTextBody
import com.example.skalelit.ui.theme.SkaleTextHead
import com.example.skalelit.ui.viewmodel.MainViewModel
import com.example.skalelit.utils.BiometricHelper

@Composable
fun AuthScreen(vm: MainViewModel) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    val activity = ctx as? FragmentActivity

    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SkaleSurface)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = "https://skalelit.com/assets/Skalelit__Logo_1_png-CDnnzTmQ.png",
                contentDescription = null,
                modifier = Modifier.width(160.dp)
            )
            Spacer(Modifier.height(32.dp))

            Text(
                if(isLogin) "Welcome Back" else "Get Started",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SkaleTextHead
            )
            Text(
                if(isLogin) "Login to manage your bookings." else "Create your account today.",
                fontSize = 14.sp,
                color = SkaleTextBody
            )
            Spacer(Modifier.height(32.dp))

            if(!isLogin) {
                SkaleInput(name, { name = it }, "Full Name")
                Spacer(Modifier.height(16.dp))
            }
            SkaleInput(email, { email = it }, "Email Address")
            Spacer(Modifier.height(16.dp))
            SkaleInput(pass, { pass = it }, "Password", true)
            Spacer(Modifier.height(32.dp))

            SkaleButton(text = if(isLogin) "Log In" else "Sign Up", onClick = {
                if(isLogin) vm.login(email, pass) { Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show() }
                else vm.signup(name, email, pass) {
                    Toast.makeText(ctx, "Account created!", Toast.LENGTH_SHORT).show()
                    isLogin = true
                }
            })

            // --- BIOMETRIC LOGIN BUTTON ---
            if (isLogin && activity != null) {
                Spacer(Modifier.height(24.dp))
                IconButton(
                    onClick = {
                        BiometricHelper.authenticate(activity) {
                            // On Success: Auto-login as admin for demo (in real app, use encrypted prefs)
                            vm.login("admin@skalelit.com", "admin123") {
                                Toast.makeText(ctx, "Biometric Auth Success", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.size(50.dp).background(SkalePrimary.copy(0.1f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Fingerprint, null, tint = SkalePrimary, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { isLogin = !isLogin }) {
                Text(if(isLogin) "Create an account" else "Log in instead", color = SkaleTextBody)
            }
        }
    }
}
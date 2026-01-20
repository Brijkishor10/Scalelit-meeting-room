package com.example.skalelit.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.skalelit.ui.theme.SkaleBackground
import com.example.skalelit.ui.viewmodel.MainViewModel

@Composable
fun AppRoot(vm: MainViewModel) {
    val user by vm.currentUser.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val showOnboarding by vm.showOnboarding.collectAsState() // <--- NEW

    Box(Modifier.fillMaxSize().background(SkaleBackground)) {
        Crossfade(targetState = isLoading, label = "AppNav") { loading ->
            if (loading) {
                SplashScreen()
            } else {
                when {
                    // 1. Show Onboarding if enabled
                    showOnboarding -> OnboardingScreen { vm.completeOnboarding() }

                    // 2. Standard Flow
                    user == null -> AuthScreen(vm)
                    user!!.role == "admin" -> AdminDashboard(vm, user!!) { vm.logout() }
                    else -> MainDashboard(vm, user!!)
                }
            }
        }
    }
}
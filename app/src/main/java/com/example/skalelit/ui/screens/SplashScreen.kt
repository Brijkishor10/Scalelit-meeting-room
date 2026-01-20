package com.example.skalelit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.skalelit.ui.theme.SkalePrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen() {
    val scale = remember { Animatable(0f) }

    // Animation Effect
    LaunchedEffect(true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = OvershootInterpolator(2f)
            )
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(SkalePrimary) // Brand Color Background
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = "https://skalelit.com/assets/Skalelit__Logo_1_png-CDnnzTmQ.png",
                contentDescription = "Logo",
                modifier = Modifier
                    .scale(scale.value)
                    .width(200.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Workspace Simplified",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.scale(scale.value)
            )
        }
    }
}

// Helper for the bounce animation
fun OvershootInterpolator(tension: Float): Easing {
    return Easing { x ->
        val t = x - 1
        t * t * ((tension + 1) * t + tension) + 1
    }
}
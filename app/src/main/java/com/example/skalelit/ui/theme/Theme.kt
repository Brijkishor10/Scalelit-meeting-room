package com.example.skalelit.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Skalelit Corporate Palette
val SkalePrimary = Color(0xFF4F46E5)
val SkaleSecondary = Color(0xFF7C3AED)
val SkaleBackground = Color(0xFFF8FAFC)
val SkaleSurface = Color(0xFFFFFFFF)
val SkaleTextHead = Color(0xFF1E293B)
val SkaleTextBody = Color(0xFF64748B)
val SkaleSuccess = Color(0xFF10B981)
val SkaleInputFill = Color(0xFFF1F5F9)

// Gradients
val SkaleGradient = Brush.horizontalGradient(listOf(SkalePrimary, SkaleSecondary))

// Theme
val NeoScheme = lightColorScheme(
    primary = SkalePrimary,
    secondary = SkaleSecondary,
    background = SkaleBackground,
    surface = SkaleSurface,
    onPrimary = Color.White,
    onBackground = SkaleTextHead,
    onSurface = SkaleTextHead
)
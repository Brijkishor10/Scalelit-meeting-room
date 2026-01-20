package com.example.skalelit.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skalelit.ui.theme.*

@Composable
fun SkaleCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val view = LocalView.current
    Column(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f))
            .clip(RoundedCornerShape(16.dp))
            .background(SkaleSurface)
            .let { if (onClick != null) it.clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            } else it }
            .padding(0.dp)
    ) {
        content()
    }
}

@Composable
fun SkaleButton(text: String, onClick: () -> Unit) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "scale")

    Box(
        Modifier
            .scale(scale)
            .fillMaxWidth()
            .height(54.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = SkalePrimary.copy(0.2f))
            .clip(RoundedCornerShape(12.dp))
            .background(SkaleGradient)
            .clickable(interactionSource, null) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun SkaleInput(value: String, onChange: (String) -> Unit, label: String, pass: Boolean = false) {
    Column {
        Text(label, color = SkaleTextBody, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.padding(start=4.dp, bottom=6.dp))
        BasicTextField(
            value = value, onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SkaleInputFill)
                .padding(horizontal = 16.dp),
            textStyle = TextStyle(color = SkaleTextHead, fontSize = 16.sp),
            visualTransformation = if (pass) PasswordVisualTransformation() else VisualTransformation.None,
            cursorBrush = SolidColor(SkalePrimary),
            singleLine = true,
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Text("Enter $label...", color = SkaleTextBody.copy(0.5f))
                    inner()
                }
            }
        )
    }
}

@Composable
fun SkaleChip(text: String, isSuccess: Boolean = true) {
    val bg = if (isSuccess) SkaleSuccess.copy(0.1f) else SkalePrimary.copy(0.1f)
    val fg = if (isSuccess) SkaleSuccess else SkalePrimary

    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text.uppercase(), color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
}
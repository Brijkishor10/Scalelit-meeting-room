package com.example.skalelit.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.skalelit.ui.theme.SkalePrimary
import com.example.skalelit.ui.theme.SkaleSurface

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF10B981)) },
        title = { Text("Confirmed!", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
        text = { Text("Your workspace has been successfully booked.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = SkalePrimary)
            }
        },
        containerColor = SkaleSurface
    )
}
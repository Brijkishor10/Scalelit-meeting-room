package com.example.skalelit.utils

import java.text.SimpleDateFormat
import java.util.*

// Extension functions for formatting
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toFormattedTime(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

fun String.toDate(): Date? {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return try {
        sdf.parse(this)
    } catch (e: Exception) {
        null
    }
}
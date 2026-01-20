package com.example.skalelit.utils



import android.Manifest
import android.R
import android.app.NotificationChannel

import android.app.NotificationManager

import android.content.Context

import android.content.pm.PackageManager

import android.os.Build
import androidx.annotation.RequiresPermission

import androidx.core.app.ActivityCompat

import androidx.core.app.NotificationCompat

import androidx.core.app.NotificationManagerCompat



class NotificationHelper(val context: Context) {

    init {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(

                "booking_channel",

                "Booking Notifications",

                NotificationManager.IMPORTANCE_DEFAULT

            )

            context.getSystemService(NotificationManager::class.java)

                .createNotificationChannel(channel)

        }

    }



    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun sendNotification(title: String, message: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&

            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)

            != PackageManager.PERMISSION_GRANTED) {

            return

        }



        NotificationManagerCompat.from(context).notify(

            System.currentTimeMillis().toInt(),

            NotificationCompat.Builder(context, "booking_channel")

                .setSmallIcon(R.drawable.ic_dialog_info)

                .setContentTitle(title)

                .setContentText(message)

                .setAutoCancel(true)

                .build()

        )

    }

}
package com.example.parsercontroller

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService

class NotificationService(val ctx: Context) {
    val CHANNEL_ID = "ws_notification"
    fun createChannel(){
        val name = "Channel name"
        var description = "Explain little about the channel for user to understand"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        }
        val notificationManager: NotificationManager = getSystemService(this.ctx,
            NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }




    @SuppressLint("MissingPermission")
    fun PushNotification(text: String){
        var builder = NotificationCompat.Builder(this.ctx,CHANNEL_ID)
            .setContentTitle("[OzonService]")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)

        with(NotificationManagerCompat.from(this.ctx)){
            notify(1,builder.build())
        }
    }
}
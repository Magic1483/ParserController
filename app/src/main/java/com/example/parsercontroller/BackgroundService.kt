package com.example.parsercontroller

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BackgroundService : Service(){
    private var isRunning = false

    companion object {
        private var instance: BackgroundService? = null

        fun getInstance(): BackgroundService? {
            return instance
        }
    }


    enum class Actions {
        START,STOP,OZON_IMPORT,OZON_UPDATE_STOCK,OZON_UPDATE_PRICES
    }

    private var notificationManager: NotificationManager? = null
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var wsClient: WebsocketClient? = null


    val channel_id = "notification_pipe"
    val notification_id = 1


    override fun onCreate() {
        super.onCreate()
        instance = this
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        CreateNotificationChannel()

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent!=null){
            if (intent.action.toString().lowercase().contains("ozon")){

                val testMode = intent.extras!!.getBoolean("testMode")
                val task = intent.action.toString()
                Log.i("BackgroundService","testMode: "+testMode.toString())
                wsStartTask(task,testMode)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }



    private fun CreateNotificationChannel(){
            val channel = NotificationChannel(
                channel_id,
                "Running Notifications",
                NotificationManager.IMPORTANCE_LOW
            )
        notificationManager?.createNotificationChannel(channel)
        notificationBuilder = NotificationCompat.Builder(this,channel_id)

        // Create the button action intent
        val actionIntent = Intent(this, CloseSessionReciever::class.java)
        val actionPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var nt = notificationBuilder!!.setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Service-1")
            .setContentText("initial state")
            .addAction(
                android.R.drawable.ic_menu_send,
                "Stop session",
                actionPendingIntent
            )
            .setOngoing(true)
            .build()

        startForeground(notification_id,nt)


    }

    fun updateNotification(content:String){
        notificationBuilder?.setContentText(content)
        notificationManager?.notify(notification_id,notificationBuilder?.build())
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun start_ws(){
        wsClient= WebsocketClient(this)
        GlobalScope.launch {
            wsClient!!.Test()
        }
    }


    fun wsStartTask(action: String,testMode:Boolean){
        wsClient= WebsocketClient(this)
        GlobalScope.launch {
            wsClient!!.StartTask(action,testMode)
        }
    }

    fun stop() {
        // stop fgService clear cache!
        wsClient?.stop()
        stopForeground(notification_id)
        Messages.Clear()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


}
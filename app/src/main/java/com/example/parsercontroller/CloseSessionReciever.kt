package com.example.parsercontroller

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.work.impl.utils.ForceStopRunnable.BroadcastReceiver

@SuppressLint("RestrictedApi")
class CloseSessionReciever :BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        BackgroundService.getInstance()?.stop()
    }
}
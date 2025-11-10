package com.example.glidedemo.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

class ListenActivityReceiver : BroadcastReceiver() {

    companion object {
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        fun register(context: Context, receiver: BroadcastReceiver) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            intentFilter.addAction(Intent.ACTION_SCREEN_ON)
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(receiver, intentFilter)
            }
        }

        fun unregister(context: Context, receiver: BroadcastReceiver) {
            context.unregisterReceiver(receiver)
        }
    }


    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action.isNullOrBlank()) return
        when (intent.action) {
            Intent.ACTION_CLOSE_SYSTEM_DIALOGS -> {
                when (intent.getStringExtra("reason")) {
                    "homekey" -> {

                    }

                    "recentapps" -> {
                    }
                }
            }

            Intent.ACTION_SCREEN_ON -> {

            }

            Intent.ACTION_SCREEN_OFF -> {
//                    App.getApp().finishAllActivity()
            }
        }
    }


}
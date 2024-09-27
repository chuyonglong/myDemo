package com.example.glidedemo.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage


class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.action)) {
            // 从 intent 中获取短信
            val messages: Array<SmsMessage> = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val sender: String = message.displayOriginatingAddress
                val content: String = message.messageBody
                // 处理短信内容
            }
        }
    }
}
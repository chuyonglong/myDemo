package com.example.glidedemo.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.databinding.ActivityTelephonyBinding


class TelephonyActivity : BaseActivity() {
    private val binding by lazy {
        ActivityTelephonyBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initActions()
    }

    private fun initActions() {
        binding.telephonyPermission.setOnClickListener {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            startActivity(intent)
        }
    }

}
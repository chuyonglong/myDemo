package com.example.glidedemo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.glidedemo.databinding.ActivityHomeBootBinding
import com.example.glidedemo.extensions.viewBindings

class HomeBootActivity : AppCompatActivity() {
    private val binding by viewBindings(ActivityHomeBootBinding::inflate)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        finish()
    }
}
package com.example.glidedemo.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import com.example.glidedemo.R
import com.example.glidedemo.databinding.ActivityCleanupFinishBinding
import com.example.glidedemo.extensions.viewBindings

class CleanupFinishActivity : AppCompatActivity() {
    private val binding by viewBindings(ActivityCleanupFinishBinding::inflate)

    @SuppressLint("StringFormatMatches")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val type = intent.getStringExtra("type")
        val sizeStr = intent.getStringExtra("sizeStr")
        val jumpHome = intent.getBooleanExtra("jumpHome", false)
        binding.tvFreeUpSpace.text =
            Html.fromHtml(String.format(getString(R.string.free_up_space, sizeStr)))
        binding.tvCleanupCompleted.text = getString(R.string.cleanup_completed, type)
        binding.cleanupFinishToolbar.setNavigationOnClickListener {
            goBack(jumpHome)
        }
        binding.tvFinishButton.setOnClickListener {
            goBack(jumpHome)
        }

    }


    private fun goBack(jumpHome: Boolean) {
        if (jumpHome) {
            startActivity(
                Intent(
                    this@CleanupFinishActivity, MainActivity::class.java
                )
            )
        }
        finish()
    }


}

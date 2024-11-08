package com.example.glidedemo.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.databinding.ActivityVaultBinding
import com.example.glidedemo.views.pattern_locker.OnPatternChangeListener
import com.example.glidedemo.views.pattern_locker.PatternLockerView
import com.example.glidedemo.views.pinlockview.PinLockListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VaultActivity : BaseActivity() {
    private val binding by lazy {
        ActivityVaultBinding.inflate(layoutInflater)
    }

    companion object {
        private var password = ""

        /**
         * 绘制解锁图案
         */
        private val text1 = "Draw an unlock pattern-绘制解锁图案"

        /**
         * 连接四个或更多点
         */
        private val text2 = "Connect 4 or more dots-连接四个或更多点"

        /**
         * 再次绘制图片
         */
        private val text3 = "Draw pattern again-再次绘制图片"

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initPatternLockerAction()
    }

    private fun initView() {
        binding.lockTipsFourDots.text = text2
        if (password.isEmpty()) {
            binding.lockTips.text = text1
        } else {

        }

        binding.pinLockView.attachIndicatorDots(binding.indicatorDots)
        binding.pinLockView.setPinLockListener(pinLockListener)
    }

    private fun initPatternLockerAction() {

        binding.lockerView.setOnPatternChangedListener(object : OnPatternChangeListener {
            override fun onStart(view: PatternLockerView) {
            }

            override fun onChange(view: PatternLockerView, hitIndexList: List<Int>) {
            }

            override fun onComplete(view: PatternLockerView, hitIndexList: List<Int>) {
                if (hitIndexList.size < 4) {
                    password = ""
                    binding.lockTips.text = text1
                    binding.lockTipsFourDots.setTextColor(Color.RED)
                    lifecycleScope.launch {
                        delay(1000)
                        binding.lockTipsFourDots.setTextColor(Color.BLACK)
                    }
                    return
                }
                val inputStr = hitIndexList.toString()
                if (password.isEmpty()) {
                    password = inputStr
                    binding.lockTips.text = text3
                } else {
                    if (inputStr == password) {
                        Toast.makeText(
                            this@VaultActivity, "设置成功密码为$password", Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        password = ""
                        binding.lockTips.text = text1
                    }
                }

            }

            override fun onClear(view: PatternLockerView) {

            }

        })
    }


    private val pinLockListener: PinLockListener = object : PinLockListener {
        override fun onComplete(pin: String) {

        }

        override fun onEmpty() {}

        override fun onPinChange(pinLength: Int, intermediatePin: String?) {}

    }


}
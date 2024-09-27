package com.example.glidedemo.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.CountDownTimer
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.glidedemo.R
import com.example.glidedemo.databinding.ViewLedClockBinding
import java.util.Calendar
import java.util.TimeZone

@SuppressLint("DefaultLocale")
class LedClockView : ConstraintLayout {
    var timingType: Int = 0
    var countdownTime: Float = 0F
    private val handler = Handler()
    private val binding: ViewLedClockBinding by lazy {
        ViewLedClockBinding.inflate(LayoutInflater.from(context))
    }


    private val timeRefresher: Runnable = object : Runnable {
        override fun run() {
            var hourStr = "00"
            var minuteStr = "00"
            var secondStr = "00"
            when (timingType) {
                0 -> {
                    val calendar = Calendar.getInstance(TimeZone.getDefault())
                    hourStr = String.format("%02d", calendar[Calendar.HOUR_OF_DAY])
                    minuteStr = String.format("%02d", calendar[Calendar.MINUTE])
                    secondStr = String.format("%02d", calendar[Calendar.SECOND])
                }

                1 -> {
                    //正计时
                    val text = formatTime(countdownTime.toLong())
                    val timeArray = text.split(":")
                    hourStr = timeArray.getOrNull(0) ?: "00"
                    minuteStr = timeArray.getOrNull(1) ?: "00"
                    secondStr = timeArray.getOrNull(2) ?: "00"
                    countdownTime++
                }

                2 -> {
                    val text = formatTime(countdownTime.toLong())
                    if (countdownTime >= 0) {
                        countdownTime--
                    } else {
                        stop()
                        return
                    }
                    val timeArray = text.split(":")
                    hourStr = timeArray.getOrNull(0) ?: "00"
                    minuteStr = timeArray.getOrNull(1) ?: "00"
                    secondStr = timeArray.getOrNull(2) ?: "00"
                }
            }
            val hour1 = hourStr.substring(0, 1)
            val hour2 = hourStr.substring(1)
            val minute1 = minuteStr.substring(0, 1)
            val minute2 = minuteStr.substring(1)
            val second1 = secondStr.substring(0, 1)
            val second2 = secondStr.substring(1)

            // 更新时间
            binding.TextOne.text = hour1
            binding.TextTwo.text = hour2
            binding.TextThree.text = ":"
            binding.TextFour.text = minute1
            binding.TextFive.text = minute2
            binding.TextSix.text = ":"
            binding.TextSeven.text = second1
            binding.TextEight.text = second2

            // 根据当前显示的文本设置 padding
            updatePadding(binding.TextOne)
            updatePadding(binding.TextTwo)
            updatePadding(binding.TextFour)
            updatePadding(binding.TextFive)
            updatePadding(binding.TextSeven)
            updatePadding(binding.TextEight)

            handler.postDelayed(this, REFRESH_DELAY.toLong())
        }

        private fun updatePadding(view: TextView) {
            val text = view.text.toString()
            val paddingThreePx = dpToPx(-3, context)
            val paddingFourPx = dpToPx(-6, context)

            when (text) {
                "3" -> view.setPadding(0, 0, paddingThreePx, 0)
                "7" -> view.setPadding(0, 0, paddingFourPx, 0)
                else -> view.setPadding(0, 0, 0, 0)  // 默认 padding
            }
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(context: Context, attrs: AttributeSet? = null) {
        val font = Typeface.createFromAsset(context.assets, FONT_DIGITAL_7)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LedClockView)
            timingType = typedArray.getInt(R.styleable.LedClockView_timingType, 0)
            countdownTime = typedArray.getFloat(R.styleable.LedClockView_countdownTime, 0F)
            typedArray.recycle()
        }
        listOf(
            binding.TextOne,
            binding.TextTwo,
            binding.TextThree,
            binding.TextFour,
            binding.TextFive,
            binding.TextSix,
            binding.TextSeven,
            binding.TextEight,
            binding.TextOneBg,
            binding.TextTwoBg,
            binding.TextThreeBg,
            binding.TextFourBg,
            binding.TextFiveBg,
            binding.TextSixBg,
            binding.TextSevenBg,
            binding.TextEightBg
        ).forEach { it.typeface = font }

        addView(binding.root)
        setViewWidthFromBg(binding.TextOne, binding.TextOneBg)
        setViewWidthFromBg(binding.TextTwo, binding.TextTwoBg)
        setViewWidthFromBg(binding.TextThree, binding.TextThreeBg)
        setViewWidthFromBg(binding.TextFour, binding.TextFourBg)
        setViewWidthFromBg(binding.TextFive, binding.TextFiveBg)
        setViewWidthFromBg(binding.TextSix, binding.TextSixBg)
        setViewWidthFromBg(binding.TextSeven, binding.TextSevenBg)
        setViewWidthFromBg(binding.TextEight, binding.TextEightBg)
    }

    private fun setViewWidthFromBg(view: TextView, bgView: TextView) {
        bgView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                bgView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val bgWidth = bgView.width
                val params = view.layoutParams as LayoutParams
                params.width = bgWidth
                view.layoutParams = params

                // 重置 padding
                view.setPadding(0, 0, 0, 0)

                val constraintSet = ConstraintSet()
                constraintSet.clone(this@LedClockView)
                constraintSet.connect(
                    view.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END
                )
                constraintSet.setMargin(view.id, ConstraintSet.END, 0)
                constraintSet.applyTo(this@LedClockView)
            }
        })
    }

    private fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }


    fun start() {
        handler.removeCallbacks(timeRefresher)
        handler.post(timeRefresher)
    }

    fun stop() {
        handler.removeCallbacks(timeRefresher)
    }


    fun formatTime(seconds: Long): String {
        if (seconds <= 0) {
            return "00:00:00"
        }
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    companion object {
        private const val FONT_DIGITAL_7 = "fonts/led.ttf"
        private const val REFRESH_DELAY = 1000
    }
}

package com.example.glidedemo.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.glidedemo.R
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class CarouselView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val images = mutableListOf<ImageView>()
    private var currentIndex = 0
    private val rotationAngle = 30f
    private val fadeInOutDuration = 500L // 渐显渐隐动画持续时间
    private val translationOffset = 100f // 图片的层叠偏移

    init {
        // 初始化三张图片
        for (i in 0..2) {
            val imageView = ImageView(context)
            imageView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP)
            images.add(imageView)
            addView(imageView)
        }

        // 默认加载三张图片
        loadImages()

        // 监听滑动手势
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (abs(distanceX) > 50) {  // 滑动超过一定距离时切换图片
                    if (distanceX > 0) {
                        nextImage()
                    } else {
                        previousImage()
                    }
                }
                return true
            }
        })

        setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadImages() {
        val drawable1 = context.getDrawable(R.drawable.img_notification_clean)
        val drawable2 = context.getDrawable(R.drawable.ic_lite_photo)
        val drawable3 = context.getDrawable(R.drawable.ic_lite_photo)

        images[0].setImageDrawable(drawable1)
        images[1].setImageDrawable(drawable2)
        images[2].setImageDrawable(drawable3)

        updateView()
    }

    private fun nextImage() {
        currentIndex = (currentIndex + 1) % images.size
        updateView()
    }

    private fun previousImage() {
        currentIndex = (currentIndex - 1 + images.size) % images.size
        updateView()
    }

    private fun updateView() {
        for (i in images.indices) {
            val imageView = images[i]
            val position = (i - currentIndex + images.size) % images.size

            // 更新每张图片的位置
            imageView.translationX = (position - 1) * translationOffset

            // 根据位置调整旋转和透明度
            imageView.rotation = position * rotationAngle.toFloat()
            imageView.alpha = when (position) {
                0 -> 1f // 中间的图片完全显示
                1 -> 0.7f // 下一张图片稍微透明
                2 -> 0.5f // 最后一张图片更透明
                else -> 0f
            }

            // 使用动画更新图片的透明度
            imageView.animate().alpha(1f).setDuration(fadeInOutDuration)
        }
    }
}

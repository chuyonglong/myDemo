package com.example.glidedemo.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.abs

class StackSliderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val MAX_VISIBLE = 3 // 最多显示的图片数量
        private const val SCALE_STEP = 0.08f // 每张图片的缩放步长
        private const val ROTATION_ANGLE = 25f // 旋转角度
    }

    private val imageViews = mutableListOf<ImageView>() // 图片视图列表
    private var lastX = 0f // 记录上次触摸的X坐标
    private var activeIndex = 0 // 当前显示的图片索引
    private var currentAnimator: ValueAnimator? = null // 当前动画
    private var adapter: Adapter? = null // 数据适配器

    init {
        initializeViews()
    }

    // 初始化视图
    private fun initializeViews() {
        repeat(MAX_VISIBLE) { index ->
            val imageView = ImageView(context).apply {
                layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).also {
                        it.setMargins(30, 30, 30, 30)
                    }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setupViewStyle(index)
            }
            imageViews.add(imageView)
            addView(imageView)
        }
        bringChildToFront(imageViews[0]) // 将最前面的视图置于顶层
    }

    // 设置视图样式
    private fun ImageView.setupViewStyle(position: Int) {
        val scale = 1 - position * SCALE_STEP
        scaleX = scale
        scaleY = scale
        translationY = position * dpToPx(10F)
        alpha = if (position == 0) 1f else 0.7f
    }

    // 处理触摸事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - lastX
                updateViewsPosition(deltaX)
                lastX = event.x
                return true
            }

            MotionEvent.ACTION_UP -> {
                handleSwipeEnd()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    // 更新视图位置
    private fun updateViewsPosition(deltaX: Float) {
        val frontView = imageViews[0]
        val progress = deltaX / width

        frontView.translationX += deltaX
        frontView.rotation = progress * ROTATION_ANGLE
        frontView.alpha = 1 - abs(progress)

        val nextView = imageViews[1]
        nextView.alpha = abs(progress)
        nextView.scaleX = 1 - SCALE_STEP + abs(progress) * SCALE_STEP
        nextView.scaleY = 1 - SCALE_STEP + abs(progress) * SCALE_STEP
    }

    // 处理滑动结束
    private fun handleSwipeEnd() {
        val frontView = imageViews[0]
        val endX = frontView.translationX

        if (abs(endX) > width / 3) {
            // 滑动距离足够，触发切换动画
//            animateTransition(if (endX > 0) 1 else -1)
            completeTransition(if (endX > 0) 1 else -1)
        } else {
            // 滑动距离不足，恢复到初始状态
            resetPosition()
        }
    }

    // 执行切换动画
    private fun animateTransition(direction: Int) {
        if (currentAnimator?.isRunning == true) return

        val outgoing = imageViews[0]
        val incoming = imageViews[1]

        currentAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener { animation ->
                val value = animation.animatedFraction

                // 当前图片向屏幕外旋转并移出
                outgoing.translationX = direction * width * value
                outgoing.rotation = direction * ROTATION_ANGLE * value
                outgoing.alpha = 1 - value

                // 下一张图片从屏幕外旋转并移入
                incoming.translationX = -direction * width * (1 - value)
                incoming.rotation = -direction * ROTATION_ANGLE * (1 - value)
                incoming.alpha = value
                incoming.scaleX = 1 - SCALE_STEP + SCALE_STEP * value
                incoming.scaleY = 1 - SCALE_STEP + SCALE_STEP * value
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    completeTransition(direction)
                }
            })
            duration = 300
            start()
        }
    }

    // 完成切换
    private fun completeTransition(direction: Int) {
        // 更新视图的层叠顺序
        if (direction > 0) {
            // 向右滑动，将最前面的视图移到最后
            val first = imageViews.removeAt(0)
            imageViews.add(first)
        } else {
            // 向左滑动，将最后面的视图移到最前面
            val last = imageViews.removeAt(imageViews.size - 1)
            imageViews.add(0, last)
        }

        // 更新 activeIndex，确保其在有效范围内
        val itemCount = adapter?.getCount() ?: 1
        activeIndex = (activeIndex + direction).mod(itemCount)

        // 重置视图位置并加载新图片
        resetViewsPosition()
        loadNewImages()
    }

    // 重置视图位置
    private fun resetViewsPosition() {
        imageViews.forEachIndexed { index, imageView ->
            imageView.translationX = 0f
            imageView.rotation = 0f
            imageView.setupViewStyle(index)
            if (index == 0) bringChildToFront(imageView)
        }
    }

    // 加载新图片
    private fun loadNewImages() {
        val adapter = adapter ?: return // 如果 adapter 为 null，直接返回
        val itemCount = adapter.getCount()
        if (itemCount == 0) return // 如果没有数据，直接返回

        val count = minOf(MAX_VISIBLE, itemCount)
        repeat(count) { i ->
            var position = (activeIndex + i) % itemCount
            if (position < 0) position += itemCount // 如果是负数，加上 itemCount 使其变为正数
            adapter.loadImage(imageViews[i], position)
        }
    }

    // 设置适配器
    fun setAdapter(adapter: Adapter) {
        this.adapter = adapter
        loadNewImages()
    }

    // 数据适配器接口
    interface Adapter {
        fun getCount(): Int
        fun loadImage(imageView: ImageView, position: Int)
    }

    // 恢复视图位置
    private fun resetPosition() {
        val frontView = imageViews[0]
        val nextView = imageViews[1]

        // 使用属性动画将 frontView 和 nextView 恢复到初始状态
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 200 // 动画时长
            addUpdateListener { animation ->
                val value = animation.animatedFraction

                // 恢复 frontView 的位置、旋转和透明度
                frontView.translationX *= (1 - value)
                frontView.rotation *= (1 - value)
                frontView.alpha = 1 - (1 - frontView.alpha) * value

                // 恢复 nextView 的缩放和透明度
                nextView.scaleX = 1 - SCALE_STEP + (nextView.scaleX - (1 - SCALE_STEP)) * (1 - value)
                nextView.scaleY = 1 - SCALE_STEP + (nextView.scaleY - (1 - SCALE_STEP)) * (1 - value)
                nextView.alpha = 0.7f + (nextView.alpha - 0.7f) * (1 - value)
            }
            start()
        }
    }

    // dp 转 px
    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
}
package com.example.glidedemo.views.pinlockview

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IntDef
import androidx.core.view.ViewCompat
import com.example.glidedemo.R
import com.example.glidedemo.views.pinlockview.ResourceUtils.Companion.getDimensionInPx

class IndicatorDots @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    @IntDef(*[IndicatorType.FIXED, IndicatorType.FILL, IndicatorType.FILL_WITH_ANIMATION])
    @Retention(
        AnnotationRetention.SOURCE
    )
    annotation class IndicatorType {
        companion object {
            const val FIXED: Int = 0
            const val FILL: Int = 1
            const val FILL_WITH_ANIMATION: Int = 2
        }
    }

    private var mDotDiameter = 0
    private var mDotSpacing = 0
    private var mFillDrawable = 0
    private var mEmptyDrawable = 0
    private var mErrorDrawable = 0

    private var mPinLength = 0
    private var mIndicatorType = 0

    private var mPreviousLength = 0

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PinLockView)

        try {
            mDotDiameter = typedArray.getDimension(
                R.styleable.PinLockView_dotDiameter,
                getDimensionInPx(getContext(), R.dimen.dp_8)
            ).toInt()
            mDotSpacing = typedArray.getDimension(
                R.styleable.PinLockView_dotSpacing,
                getDimensionInPx(getContext(), R.dimen.dp_8)
            ).toInt()
            mFillDrawable = typedArray.getResourceId(
                R.styleable.PinLockView_dotFilledBackground,
                R.drawable.bg_dot_filled
            )
            mErrorDrawable = typedArray.getResourceId(
                R.styleable.PinLockView_dotErrorBackground,
                R.drawable.bg_dot_error
            )
            mEmptyDrawable = typedArray.getResourceId(
                R.styleable.PinLockView_dotEmptyBackground,
                R.drawable.bg_dot_empty
            )
            mPinLength = typedArray.getInt(R.styleable.PinLockView_pinLength, DEFAULT_PIN_LENGTH)
            mIndicatorType = typedArray.getInt(
                R.styleable.PinLockView_indicatorType,
                IndicatorType.FIXED
            )
        } finally {
            typedArray.recycle()
        }

        initView(context)
    }

    private fun initView(context: Context) {
        ViewCompat.setLayoutDirection(this, ViewCompat.LAYOUT_DIRECTION_LTR)
        if (mIndicatorType == 0) {
            for (i in 0 until mPinLength) {
                val dot = View(context)
                emptyDot(dot)

                val params = LayoutParams(
                    mDotDiameter,
                    mDotDiameter
                )
                params.setMargins(mDotSpacing, 0, mDotSpacing, 0)
                dot.layoutParams = params

                addView(dot)
            }
        } else if (mIndicatorType == 2) {
            layoutTransition = LayoutTransition()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // If the indicator type is not fixed
        if (mIndicatorType != 0) {
            val params = this.layoutParams
            params.height = mDotDiameter
            requestLayout()
        }
    }

    fun updateDot(length: Int) {
        if (mIndicatorType == 0) {
            if (length > 0) {
                if (length > mPreviousLength) {
                    fillDot(getChildAt(length - 1))
                } else {
                    emptyDot(getChildAt(length))
                }
                mPreviousLength = length
            } else {
                // When {@code mPinLength} is 0, we need to reset all the views back to empty
                for (i in 0 until childCount) {
                    val v = getChildAt(i)
                    emptyDot(v)
                }
                mPreviousLength = 0
            }
        } else {
            if (length > 0) {
                if (length > mPreviousLength) {
                    val dot = View(context)
                    fillDot(dot)

                    val params = LayoutParams(
                        mDotDiameter,
                        mDotDiameter
                    )
                    params.setMargins(mDotSpacing, 0, mDotSpacing, 0)
                    dot.layoutParams = params

                    addView(dot, length - 1)
                } else {
                    removeViewAt(length)
                }
                mPreviousLength = length
            } else {
                removeAllViews()
                mPreviousLength = 0
            }
        }
    }

    private fun emptyDot(dot: View) {
        dot.setBackgroundResource(mEmptyDrawable)
    }

    private fun fillDot(dot: View) {
        dot.setBackgroundResource(mFillDrawable)
    }

    fun errorDot() {
        for (i in 0 until mPinLength) {
            val view = getChildAt(i)
            view.setBackgroundResource(mErrorDrawable)
        }
    }


    fun getPinLength(): Int {
        return mPinLength
    }

    fun setPinLength(pinLength: Int) {
        this.mPinLength = pinLength
        removeAllViews()
        initView(context)
    }

    @get:IndicatorType
    var indicatorType: Int
        get() = mIndicatorType
        set(type) {
            this.mIndicatorType = type
            removeAllViews()
            initView(context)
        }

    companion object {
        private const val DEFAULT_PIN_LENGTH = 4
    }
}

package com.example.glidedemo.views

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import android.text.TextPaint
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import com.example.glidedemo.R


class GradientTextView(
    context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {
    enum class GradientDirection {
        HORIZONTAL, VERTICAL
    }

    private var mPaint: TextPaint? = null
    private var mLinearGradient: LinearGradient? = null
    private var mMeasureWidth = 0
    private var mTextMatrix: Matrix? = null

    @ColorInt
    var startColor: Int = 0xFF333333.toInt()

    @ColorInt
    var endColor: Int = 0xFF333333.toInt()

    private var mGradientDirection: GradientDirection = GradientDirection.HORIZONTAL

    init {
        if (attrs != null) {
            val attrArray = getContext().obtainStyledAttributes(attrs, R.styleable.GradientTextView)
            startColor = attrArray.getColor(R.styleable.GradientTextView_startColor, startColor)
            endColor = attrArray.getColor(R.styleable.GradientTextView_endColor, endColor)
            mGradientDirection =
                when (attrArray.getInt(R.styleable.GradientTextView_gradientDirection, 0)) {
                    0 -> GradientDirection.HORIZONTAL
                    else -> GradientDirection.VERTICAL
                }
            attrArray.recycle()
        }
    }

//    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        super.onSizeChanged(w, h, oldw, oldh)
//        if (w == 0 || h == 0) {
//            return
//        }
//        mPaint = paint
//        mLinearGradient = if (mGradientDirection == GradientDirection.HORIZONTAL) {
//            LinearGradient(
//                0f,
//                0f,
//                w.toFloat(),
//                0f,
//                intArrayOf(mStartColor, mEndColor),
//                null,
//                Shader.TileMode.CLAMP
//            )
//        } else {
//            LinearGradient(
//                0f,
//                0f,
//                0f,
//                h.toFloat(),
//                intArrayOf(mStartColor, mEndColor),
//                null,
//                Shader.TileMode.CLAMP
//            )
//        }
//        mPaint?.shader = mLinearGradient
//        mTextMatrix = Matrix()
//        invalidate()
//    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) {
            return
        }
        mPaint = paint
        mLinearGradient = if (mGradientDirection == GradientDirection.HORIZONTAL) {
            LinearGradient(
                0f,
                0f,
                w.toFloat(),
                0f,
                intArrayOf(startColor, endColor),
                null,
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                0f,
                0f,
                0f,
                h.toFloat(),
                intArrayOf(startColor, endColor),
                null,
                Shader.TileMode.CLAMP
            )
        }
        mPaint?.shader = mLinearGradient
        mTextMatrix = Matrix()
        invalidate()
    }


}




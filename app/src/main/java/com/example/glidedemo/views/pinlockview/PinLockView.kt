package com.example.glidedemo.views.pinlockview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.example.glidedemo.R
import com.example.glidedemo.views.pinlockview.ResourceUtils.Companion.getDimensionInPx


class PinLockView : RecyclerView {
    private var mPin = ""
    private var mPinLength = 0
    private var mHorizontalSpacing = 0
    private var mVerticalSpacing = 0
    private var mTextColor = 0
    private var mDeleteButtonPressedColor = 0
    private var mTextSize = 0
    private var mButtonSize = 0
    private var mDeleteButtonSize = 0
    private var mButtonBackgroundDrawable: Drawable? = null
    private var mDeleteButtonDrawable: Drawable? = null
    private var mShowDeleteButton = false

    private var mIndicatorDots: IndicatorDots? = null
    private val mAdapter: PinLockAdapter by lazy {
        PinLockAdapter()
    }
    private var mPinLockListener: PinLockListener? = null
    private val mCustomizationOptionsBundle by lazy {
        CustomizationOptionsBundle()
    }

    private val mOnNumberClickListener: PinLockAdapter.OnNumberClickListener = object :
        PinLockAdapter.OnNumberClickListener {
        override fun onNumberClicked(keyValue: Int) {
            if (mPin.length < mPinLength) {
                mPin += keyValue.toString()
                mIndicatorDots?.updateDot(mPin.length)

                if (mPin.length == 1) {
                    mAdapter.pinLength = mPin.length
                    mAdapter.notifyItemChanged(mAdapter.itemCount - 1)
                }

                if (mPin.length == mPinLength) {
                    mPinLockListener?.onComplete(mPin)
                } else {
                    mPinLockListener?.onPinChange(mPin.length, mPin)
                }
            } else {
                if (!isShowDeleteButton) {
                    resetPinLockView()
                    mPin += keyValue.toString()

                    mIndicatorDots?.updateDot(mPin.length)

                    mPinLockListener?.onPinChange(mPin.length, mPin)
                } else {
                    mPinLockListener?.onComplete(mPin)
                }
            }
        }
    }

    private val mOnDeleteClickListener: PinLockAdapter.OnDeleteClickListener = object :
        PinLockAdapter.OnDeleteClickListener {
        override fun onDeleteClicked() {
            if (mPin.isNotEmpty()) {
                mPin = mPin.substring(0, mPin.length - 1)
                mIndicatorDots?.updateDot(mPin.length)
                mAdapter.pinLength = mPin.length
                mAdapter.notifyItemChanged(mAdapter.itemCount - 1)
                if (mPin.isEmpty()) {
                    mPinLockListener?.onEmpty()
                    clearInternalPin()
                } else {
                    mPinLockListener?.onPinChange(mPin.length, mPin)
                }
            } else {
                mPinLockListener?.onEmpty()
            }
        }

        override fun onDeleteLongClicked() {
            resetPinLockView()
            mPinLockListener?.onEmpty()
        }
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    ) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attributeSet: AttributeSet?, defStyle: Int) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PinLockView)

        try {
            mPinLength = typedArray.getInt(R.styleable.PinLockView_pinLength, DEFAULT_PIN_LENGTH)
            mHorizontalSpacing = typedArray.getDimension(
                R.styleable.PinLockView_keypadHorizontalSpacing, getDimensionInPx(
                    context, R.dimen.dp_32
                )
            ).toInt()
            mVerticalSpacing = typedArray.getDimension(
                R.styleable.PinLockView_keypadVerticalSpacing, getDimensionInPx(
                    context, R.dimen.dp_0
                )
            ).toInt()
            mTextColor = typedArray.getColor(
                R.styleable.PinLockView_keypadTextColor, getColor(
                    context, R.color.white
                )
            )
            mTextSize = typedArray.getDimension(
                R.styleable.PinLockView_keypadTextSize, getDimensionInPx(
                    context, R.dimen.sp_16
                )
            ).toInt()
            mButtonSize = typedArray.getDimension(
                R.styleable.PinLockView_keypadButtonSize, getDimensionInPx(
                    context, R.dimen.dp_60
                )
            ).toInt()
            mDeleteButtonSize = typedArray.getDimension(
                R.styleable.PinLockView_keypadDeleteButtonSize, getDimensionInPx(
                    context, R.dimen.dp_16
                )
            ).toInt()
            mButtonBackgroundDrawable =
                typedArray.getDrawable(R.styleable.PinLockView_keypadButtonBackgroundDrawable)
            mDeleteButtonDrawable =
                typedArray.getDrawable(R.styleable.PinLockView_keypadDeleteButtonDrawable)
            mShowDeleteButton =
                typedArray.getBoolean(R.styleable.PinLockView_keypadShowDeleteButton, true)
            mDeleteButtonPressedColor = typedArray.getColor(
                R.styleable.PinLockView_keypadDeleteButtonPressedColor, ResourceUtils.getColor(
                    context, R.color.greyish
                )
            )
        } finally {
            typedArray.recycle()
        }

        mCustomizationOptionsBundle.textColor = mTextColor
        mCustomizationOptionsBundle.textSize = mTextSize
        mCustomizationOptionsBundle.buttonSize = mButtonSize
        mCustomizationOptionsBundle.buttonBackgroundDrawable = mButtonBackgroundDrawable
        mCustomizationOptionsBundle.deleteButtonDrawable = mDeleteButtonDrawable
        mCustomizationOptionsBundle.deleteButtonSize = mDeleteButtonSize
        mCustomizationOptionsBundle.isShowDeleteButton = mShowDeleteButton
        mCustomizationOptionsBundle.deleteButtonPressesColor = mDeleteButtonPressedColor

        initView()
    }

    private fun initView() {
        layoutManager = LTRGridLayoutManager(context, 3)

        mAdapter.onItemClickListener = mOnNumberClickListener
        mAdapter.onDeleteClickListener = mOnDeleteClickListener
        mAdapter.customizationOptions = mCustomizationOptionsBundle
        adapter = mAdapter

//        addItemDecoration(ItemSpaceDecoration(mHorizontalSpacing, mVerticalSpacing, 3, false))
        overScrollMode = OVER_SCROLL_NEVER
    }


    fun setPinLockListener(pinLockListener: PinLockListener?) {
        this.mPinLockListener = pinLockListener
    }


    fun getPinLength(): Int {
        return mPinLength
    }

    fun setPinLength(pinLength: Int) {
        this.mPinLength = pinLength
    }


    var textColor: Int
        get() = mTextColor
        @SuppressLint("NotifyDataSetChanged") set(textColor) {
            this.mTextColor = textColor
            mCustomizationOptionsBundle.textColor = textColor
            mAdapter.notifyDataSetChanged()
        }

    var textSize: Int
        get() = mTextSize
        @SuppressLint("NotifyDataSetChanged") set(textSize) {
            this.mTextSize = textSize
            mCustomizationOptionsBundle.textSize = textSize
            mAdapter.notifyDataSetChanged()
        }

    var buttonSize: Int
        get() = mButtonSize
        @SuppressLint("NotifyDataSetChanged") set(buttonSize) {
            this.mButtonSize = buttonSize
            mCustomizationOptionsBundle.buttonSize = buttonSize
            mAdapter.notifyDataSetChanged()
        }

    var buttonBackgroundDrawable: Drawable?
        get() = mButtonBackgroundDrawable
        @SuppressLint("NotifyDataSetChanged") set(buttonBackgroundDrawable) {
            this.mButtonBackgroundDrawable = buttonBackgroundDrawable
            mCustomizationOptionsBundle.buttonBackgroundDrawable = buttonBackgroundDrawable
            mAdapter.notifyDataSetChanged()
        }

    var deleteButtonDrawable: Drawable?
        get() = mDeleteButtonDrawable
        @SuppressLint("NotifyDataSetChanged") set(deleteBackgroundDrawable) {
            this.mDeleteButtonDrawable = deleteBackgroundDrawable
            mCustomizationOptionsBundle.deleteButtonDrawable = deleteBackgroundDrawable
            mAdapter.notifyDataSetChanged()
        }


    var isShowDeleteButton: Boolean
        get() = mShowDeleteButton
        @SuppressLint("NotifyDataSetChanged") set(showDeleteButton) {
            this.mShowDeleteButton = showDeleteButton
            mCustomizationOptionsBundle.isShowDeleteButton = showDeleteButton
            mAdapter.notifyDataSetChanged()
        }

    var deleteButtonPressedColor: Int
        get() = mDeleteButtonPressedColor
        @SuppressLint("NotifyDataSetChanged") set(deleteButtonPressedColor) {
            this.mDeleteButtonPressedColor = deleteButtonPressedColor
            mCustomizationOptionsBundle.deleteButtonPressesColor = deleteButtonPressedColor
            mAdapter.notifyDataSetChanged()
        }


    private fun clearInternalPin() {
        mPin = ""
    }


    fun resetPinLockView() {
        clearInternalPin()

        mAdapter.pinLength = mPin.length
        mAdapter.notifyItemChanged(mAdapter.itemCount - 1)

        mIndicatorDots?.updateDot(mPin.length)
    }


    fun attachIndicatorDots(mIndicatorDots: IndicatorDots?) {
        this.mIndicatorDots = mIndicatorDots
        mIndicatorDots?.setPinLength(mPinLength)
    }

    fun errorDots() {
        mIndicatorDots?.errorDot()
    }

    companion object {
        private const val DEFAULT_PIN_LENGTH = 4
    }
}

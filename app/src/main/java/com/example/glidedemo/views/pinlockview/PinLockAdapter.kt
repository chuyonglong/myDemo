package com.example.glidedemo.views.pinlockview

import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.glidedemo.databinding.LayoutDeleteItemBinding
import com.example.glidedemo.databinding.ViewNumberItemBinding


class PinLockAdapter : RecyclerView.Adapter<ViewHolder>() {
    var customizationOptions: CustomizationOptionsBundle? = null
    var onItemClickListener: OnNumberClickListener? = null
    var onDeleteClickListener: OnDeleteClickListener? = null
    var pinLength: Int = 0

    private val mKeyValues: IntArray by lazy {
        getAdjustKeyValues(intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0))
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder: ViewHolder
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == VIEW_TYPE_NUMBER) {
            val binding = ViewNumberItemBinding.inflate(inflater, parent, false)
            viewHolder = NumberViewHolder(binding)
        } else {
            val binding = LayoutDeleteItemBinding.inflate(inflater, parent, false)
            viewHolder = DeleteViewHolder(binding)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.itemViewType == VIEW_TYPE_NUMBER) {
            (holder as NumberViewHolder).configureNumberButtonHolder(position)
        }
    }


    override fun getItemCount(): Int {
        return 12
    }

    override fun getItemViewType(position: Int): Int {
        if (position == itemCount - 1) {
            return VIEW_TYPE_DELETE
        }
        return VIEW_TYPE_NUMBER
    }

    private fun getAdjustKeyValues(keyValues: IntArray): IntArray {
        val adjustedKeyValues = IntArray(keyValues.size + 1)
        for (i in keyValues.indices) {
            if (i < 9) {
                adjustedKeyValues[i] = keyValues[i]
            } else {
                adjustedKeyValues[i] = -1
                adjustedKeyValues[i + 1] = keyValues[i]
            }
        }
        return adjustedKeyValues
    }

    inner class NumberViewHolder(val binding: ViewNumberItemBinding) : ViewHolder(binding.root) {
        init {
            binding.textNum.setOnClickListener {
                mKeyValues.getOrNull(absoluteAdapterPosition)?.let {
                    onItemClickListener?.onNumberClicked(it)
                }
            }

        }

        fun configureNumberButtonHolder(position: Int) {
            if (position == 9) {
                binding.textNum.visibility = View.GONE
            } else {
                binding.textNum.text = mKeyValues[position].toString()
                binding.textNum.visibility = View.VISIBLE
                binding.textNum.tag = mKeyValues[position]
            }

            if (customizationOptions != null) {
                binding.textNum.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    customizationOptions!!.textSize.toFloat()
                )
//                val params = LinearLayout.LayoutParams(
//                    customizationOptions!!.buttonSize,
//                    customizationOptions!!.buttonSize
//                )
//                binding.textNum.layoutParams = params
            }
        }
    }

    inner class DeleteViewHolder @SuppressLint("ClickableViewAccessibility") constructor(binding: LayoutDeleteItemBinding) :
        ViewHolder(binding.root) {
        init {
//            if (customizationOptions!!.isShowDeleteButton && pinLength > 0) {
            binding.root.setOnClickListener {
                onDeleteClickListener?.onDeleteClicked()
            }

            binding.root.setOnLongClickListener {
                onDeleteClickListener?.onDeleteLongClicked()
                true
            }

            binding.root.setOnTouchListener(object : OnTouchListener {
                private var rect: Rect? = null
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        binding.buttonImage.setColorFilter(
                            customizationOptions!!.deleteButtonPressesColor
                        )
                        rect = Rect(v.left, v.top, v.right, v.bottom)
                    }
                    if (event.action == MotionEvent.ACTION_UP) {
                        binding.buttonImage.clearColorFilter()
                    }
                    if (event.action == MotionEvent.ACTION_MOVE) {
                        if (!rect!!.contains(
                                v.left + event.x.toInt(),
                                v.top + event.y.toInt()
                            )
                        ) {
                            binding.buttonImage.clearColorFilter()
                        }
                    }
                    return false
                }
            })
//            }
        }
    }

    interface OnNumberClickListener {
        fun onNumberClicked(keyValue: Int)
    }

    interface OnDeleteClickListener {
        fun onDeleteClicked()

        fun onDeleteLongClicked()
    }

    companion object {
        private const val VIEW_TYPE_NUMBER = 0
        private const val VIEW_TYPE_DELETE = 1
    }
}

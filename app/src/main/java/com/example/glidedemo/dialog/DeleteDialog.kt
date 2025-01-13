package com.example.glidedemo.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.glidedemo.databinding.DialogDeleteBinding
import com.example.glidedemo.dto.DialogDTO
import com.example.glidedemo.utils.DialogClickEnum
import com.example.glidedemo.utils.beGone
import com.example.glidedemo.utils.beInVisibleIf


class DeleteDialog(
    activity: Activity,
    dto: DialogDTO,
    val callback: (clickType: DialogClickEnum) -> Unit
) : Dialog(activity) {
    private val binding by lazy {
        DialogDeleteBinding.inflate(activity.layoutInflater).apply {
            if (dto.title != -1) {
                dialogTitle.text = activity.getString(dto.title)
            } else {
                dialogTitle.beGone()
            }

            dialogClose.beInVisibleIf(!dto.isShowClose)
            dialogContent.text = dto.content
            dialogCancel.text = activity.getString(dto.cancelText)
            dialogOk.text = activity.getString(dto.okText)

        }
    }

    init {
        setContentView(binding.root)
        binding.dialogClose.setOnClickListener {
            this.dismiss()
        }
        binding.dialogCancel.setOnClickListener {
            this.dismiss()
            callback(DialogClickEnum.CANCEL)
        }
        binding.dialogOk.setOnClickListener {
            this.dismiss()
            callback(DialogClickEnum.OK)

        }
        val window = window
        window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
        val layoutParams = window?.attributes
        layoutParams?.width = ConstraintLayout.LayoutParams.MATCH_PARENT // 设置宽度为屏幕宽度
        layoutParams?.height = ConstraintLayout.LayoutParams.WRAP_CONTENT // 设置高度为自适应内容
        window?.attributes = layoutParams

    }

}
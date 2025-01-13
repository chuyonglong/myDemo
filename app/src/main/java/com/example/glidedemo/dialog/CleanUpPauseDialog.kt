package com.example.glidedemo.dialog

import android.app.Activity
import android.graphics.Color
import android.view.ViewGroup
import com.example.glidedemo.R
import com.example.glidedemo.databinding.DialogCleanUpPauseBinding
import com.example.glidedemo.utils.DialogClickEnum
import com.google.android.material.bottomsheet.BottomSheetDialog


class CleanUpPauseDialog(
    activity: Activity,
    val callback: (clickType: DialogClickEnum) -> Unit
) {
    init {
        val bottomSheetDialog = BottomSheetDialog(activity, R.style.BottomSheetDialogStyle)
        DialogCleanUpPauseBinding.inflate(activity.layoutInflater).apply {
            bottomSheetDialog.setContentView(this.root)
            bottomSheetDialog.show()
            (root.parent as ViewGroup).setBackgroundColor(Color.TRANSPARENT)
            cleanupCancel.setOnClickListener {
                bottomSheetDialog.dismiss()
                callback(DialogClickEnum.CANCEL)
            }
            cleanupConfirm.setOnClickListener {
                bottomSheetDialog.dismiss()
                callback(DialogClickEnum.OK)
            }
            bottomSheetDialog.setOnCancelListener {
                bottomSheetDialog.dismiss()
                callback(DialogClickEnum.CANCEL)
            }
        }
    }
}

package com.example.glidedemo.dialog

import android.content.Context
import android.os.Bundle
import com.example.glidedemo.R
import com.example.glidedemo.databinding.DialogAppInstallBinding
import com.example.glidedemo.utils.DialogClickEnum
import com.google.android.material.bottomsheet.BottomSheetDialog

class AppInstallDialog(
    context: Context,
    val packageName: String,
    val callback: (clickType: DialogClickEnum) -> Unit
) : BottomSheetDialog(context, R.style.BottomSheetDialogStyle) {

    private val binding by lazy {
        DialogAppInstallBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()

    }

    private fun initView() {
        binding.cleanupTitle.text = packageName
    }

}
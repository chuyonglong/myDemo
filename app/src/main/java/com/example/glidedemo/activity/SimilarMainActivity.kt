package com.example.glidedemo.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.glidedemo.databinding.ActivitySimilarMainBinding
import com.example.glidedemo.databinding.FlowlayoutTextBinding
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.permission.GalleryPermissionUtils
import com.example.glidedemo.views.flowlayout.FlowLayout
import com.example.glidedemo.views.flowlayout.TagAdapter
import com.example.glidedemo.views.flowlayout.TagFlowLayout

class SimilarMainActivity : AppCompatActivity(), TagFlowLayout.OnTagClickListener,
    TagFlowLayout.OnSelectListener {
    private val binding by viewBindings(ActivitySimilarMainBinding::inflate)

    private val mVals by lazy {
        linkedMapOf(
            "0" to "0:图像分类Interpreter",
            "1" to "1:图像分类",
            "2" to "2:mlkit图像标签",
            "3" to "3:相似图片",
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initFlowLayout()
    }

    private fun initFlowLayout() {
        binding.tagFlowLayout.apply {
            addAdapter(object : TagAdapter<Any>(mVals.values.toList()) {
                override fun getView(parent: FlowLayout?, position: Int, s: Any): View {
                    val tvBinding =
                        FlowlayoutTextBinding.inflate(layoutInflater, binding.tagFlowLayout, false)
                    tvBinding.root.text = s.toString()
                    return tvBinding.root
                }
            })
        }
        binding.tagFlowLayout.apply {
            setOnTagClickListener(this@SimilarMainActivity)
            setOnSelectListener(this@SimilarMainActivity)
        }
    }

    override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
        when (position) {
            0 -> {
                startActivity(Intent(this, ImageClassificationInterpreterActivity::class.java))
            }

            1 -> {
                startActivity(Intent(this, ImageClassificationActivity::class.java))
            }

            2 -> {
                startActivity(Intent(this, ImageLabelingActivity::class.java))
            }

            3 -> {
                val permissionEnum = GalleryPermissionUtils.checkMediaPermissionResult(this)
                if (permissionEnum == GalleryPermissionUtils.PermissionEnum.NO_PERMISSIONS) {
                    toast("没有权限----,请求权限!!!")
                    GalleryPermissionUtils.requestMediaPermissions(similarPermissionLauncher)
                } else {
                    startActivity(Intent(this, SimilarScanActivity::class.java))
                }
            }
        }
        return true
    }

    override fun onSelected(selectPosSet: Set<Int>?) {}

    /**
     * 检查图片视频权限
     */
    private val similarPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.isNotEmpty()) {
                startActivity(Intent(this, SimilarScanActivity::class.java))
            }
        }
}
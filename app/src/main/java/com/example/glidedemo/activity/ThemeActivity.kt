package com.example.glidedemo.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.glidedemo.databinding.ActivityThemeBinding
import com.example.glidedemo.databinding.FlowlayoutTextBinding
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.views.flowlayout.FlowLayout
import com.example.glidedemo.views.flowlayout.TagAdapter
import com.example.glidedemo.views.flowlayout.TagFlowLayout
import com.google.android.material.color.DynamicColors

class ThemeActivity : AppCompatActivity(), TagFlowLayout.OnTagClickListener {
    private val binding by viewBindings(ActivityThemeBinding::inflate)

    private val mVals by lazy {
        linkedMapOf(
            "0" to "全屏主题",
            "1" to "",

        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivitiesIfAvailable(application)
        binding
        initViews()
        initFlowLayout()
        initActions()
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
    }

    private fun initViews() {
        val currentTheme = AppCompatDelegate.getDefaultNightMode()
        binding.themeTag.text = when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_YES -> "当前为:黑暗模式"
            AppCompatDelegate.MODE_NIGHT_NO -> "当前为:明亮模式"
            else -> "当前为:跟随系统"
        }
    }

    private fun initActions() {
        binding.tagFlowLayout.setOnTagClickListener(this)
    }

    override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
        when (position) {
            0 -> {

            }

            1 -> {

            }


        }
        return true
    }


}

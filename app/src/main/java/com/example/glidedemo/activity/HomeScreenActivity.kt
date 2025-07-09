package com.example.glidedemo.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.glidedemo.databinding.ActivityHomeScreenBinding
import com.example.glidedemo.databinding.FlowlayoutTextBinding
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.views.flowlayout.FlowLayout
import com.example.glidedemo.views.flowlayout.TagAdapter
import com.example.glidedemo.views.flowlayout.TagFlowLayout

class HomeScreenActivity : AppCompatActivity(), TagFlowLayout.OnTagClickListener,
    TagFlowLayout.OnSelectListener {
    private val binding by viewBindings(ActivityHomeScreenBinding::inflate)


    private val mVals by lazy {
        linkedMapOf(
            "0" to "0:",
            "1" to "1:",
            "2" to "2:",
            "3" to "3:",
            "4" to "4:",
            "5" to "5:",
            "6" to "6:",
            "7" to "7:",
            "8" to "8:",
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initViews()
        initFlowLayout()
    }


    private fun initViews() {
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
            setOnTagClickListener(this@HomeScreenActivity)
            setOnSelectListener(this@HomeScreenActivity)
        }
    }


    override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
        when (position) {
            0 -> {}
            1 -> {}
            2 -> {}
            3 -> {}
            4 -> {}
            5 -> {}


        }
        return true
    }

    override fun onSelected(selectPosSet: Set<Int>?) {}

}
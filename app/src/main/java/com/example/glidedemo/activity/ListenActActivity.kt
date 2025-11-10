package com.example.glidedemo.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.glidedemo.databinding.ActivityListenActBinding
import com.example.glidedemo.databinding.FlowlayoutTextBinding
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.receiver.ListenActivityReceiver
import com.example.glidedemo.service.ListenActivityForegroundService
import com.example.glidedemo.service.ListenActivityService
import com.example.glidedemo.views.flowlayout.FlowLayout
import com.example.glidedemo.views.flowlayout.TagAdapter
import com.example.glidedemo.views.flowlayout.TagFlowLayout
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.getValue

class ListenActActivity : AppCompatActivity(), TagFlowLayout.OnTagClickListener,
    TagFlowLayout.OnSelectListener {

    private val TAG = "ListenActActivity"


    private val listenActivityReceiver = ListenActivityReceiver()

    private val binding by viewBindings(ActivityListenActBinding::inflate)
    private val mVals by lazy {
        linkedMapOf(
            "0" to "0:普通service前台启动",
            "1" to "1:普通service后台启动",
            "2" to "2:前台service前台启动",
            "3" to "3:前台service后台启动",

            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initFlowLayout()
        listenEdittext()
        ListenActivityReceiver.register(this, listenActivityReceiver)
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
            setOnTagClickListener(this@ListenActActivity)
            setOnSelectListener(this@ListenActActivity)
        }
    }

    override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
        var text = binding.editText.text.toString()
        if (text.isEmpty()) {
            text = "1"
        }
        Log.d(TAG, "onTagClick: $position, $text")
        val currentTime = System.currentTimeMillis()
        when (position) {
            0 -> {
                toast("普通service前台启动activity")

                ListenActivityService.startByAction(
                    this@ListenActActivity,
                    ListenActivityService.START_ACTIVITY_MESSAGE_IN_FOREGROUND,
                    text,
                    currentTime
                )
            }

            1 -> {
                toast("普通service后台启动activity")
                ListenActivityService.startByAction(
                    this@ListenActActivity,
                    ListenActivityService.START_ACTIVITY_MESSAGE_IN_BACKGROUND,
                    text,
                    currentTime
                )
            }

            2 -> {
                toast("前台service前台启动activity")
                ListenActivityForegroundService.startByAction(
                    this@ListenActActivity,
                    ListenActivityForegroundService.START_IN_FOREGROUND,
                    text,
                    currentTime
                )
            }

            3 -> {
                toast("前台service后台启动activity")
                ListenActivityForegroundService.startByAction(
                    this@ListenActActivity,
                    ListenActivityForegroundService.START_IN_BACKGROUND,
                    text,
                    currentTime
                )
            }

            4 -> {}
            5 -> {}
            6 -> {}
            7 -> {}
            8 -> {}
            9 -> {}

        }
        return true
    }

    override fun onSelected(selectPosSet: Set<Int>?) {}

    override fun onDestroy() {
        super.onDestroy()
        ListenActivityReceiver.unregister(this, listenActivityReceiver)
    }


    private fun listenEdittext() {
        binding.editText.addTextChangedListener(object : TextWatcher {
            var l: Int = 0

            /**/////记录字符串被删除字符之前，字符串的长度 */
            var location: Int = 0 //记录光标的位置

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                l = s.length
                location = binding.editText.selectionStart
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val p: Pattern = Pattern.compile("^(100|[1-9]\\d|\\d)$")

                val m: Matcher = p.matcher(s.toString())
                if (m.find() || ("") == s.toString()) {
                } else {
                    toast("请输入正确的数值")
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


}
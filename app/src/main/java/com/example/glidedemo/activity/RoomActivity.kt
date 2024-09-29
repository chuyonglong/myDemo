package com.example.glidedemo.activity

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.adapter.RoomAdapter
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.databinding.ActivityRoomBinding
import com.example.glidedemo.entity.TextData
import com.example.glidedemo.extensions.textDB
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomActivity : BaseActivity() {

    private val binding by lazy {
        ActivityRoomBinding.inflate(layoutInflater)
    }

    private val roomAdapter by lazy {
        RoomAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initActions()
        initAdapter()
    }

    private fun initActions() {
        binding.roomSave.setOnClickListener {
            val text = binding.roomEdit.text?.toString()
            if (text != null && !TextUtils.isEmpty(text)) {
                lifecycleScope.launch(Dispatchers.IO) {
                    textDB.insert(TextData(null, text))
                    withContext(Dispatchers.Main) {
                        binding.roomEdit.setText("")
                    }
                }
            } else {
                Toast.makeText(this@RoomActivity, "请输入数据!", Toast.LENGTH_SHORT).show()
            }

        }

        binding.roomGet.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val list = textDB.getAll()
                withContext(Dispatchers.Main) {
                    roomAdapter.setRoomList(list.toMutableList())
                }
            }


        }
    }

    private fun initAdapter() {
        binding.roomGrid.apply {
            adapter = roomAdapter
            layoutManager = FlexboxLayoutManager(this@RoomActivity)
        }
    }


}
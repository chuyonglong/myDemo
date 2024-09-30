package com.example.glidedemo.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.adapter.LineAdapter
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.databinding.ActivityListAdapterBinding
import com.example.glidedemo.extensions.textDB
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListAdapterActivity : BaseActivity() {

    private val binding by lazy {
        ActivityListAdapterBinding.inflate(layoutInflater)
    }

    private val lineAdapter by lazy {
        LineAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initActions()
        initAdapter()
    }

    private fun initAdapter() {
        binding.listAdapterGrid.apply {
            adapter = lineAdapter
            layoutManager = FlexboxLayoutManager(this@ListAdapterActivity)
        }
    }

    private fun initActions() {
        binding.listAdapterGetData.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val list = textDB.getAll()
                withContext(Dispatchers.Main) {
                    lineAdapter.submitList(list)
                }
            }


        }
    }

}
package com.example.glidedemo.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.adapter.CeilingAdapter
import com.example.glidedemo.bean.MediaData
import com.example.glidedemo.databinding.ActivityCeilingBinding
import com.example.glidedemo.extensions.TAG
import com.example.glidedemo.utils.MediaQueryForPermission
import com.example.glidedemo.views.GalleryGridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CeilingActivity : AppCompatActivity() {

    private val ceilingAdapter by lazy {
        CeilingAdapter()
    }


    private val binding by lazy {
        ActivityCeilingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initAdapter()
        initData()
    }

    private fun initAdapter() {
        binding.recycler.adapter = ceilingAdapter
        binding.recycler.layoutManager = GalleryGridLayoutManager(this, 4)

    }

    private fun initData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = MediaQueryForPermission.queryAllData(this@CeilingActivity)
            Log.d(TAG, "initData: ----${list.size}")

            if (list.isNotEmpty()) {
                // 只取前 100 条数据
                val finalList =
                    list.filterIsInstance<MediaData>().take(100) as MutableList<MediaData>

                withContext(Dispatchers.Main) {
                    ceilingAdapter.setMediaList(finalList)
                }
            }
        }
    }

}
package com.example.glidedemo.activity

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.ImageSource
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.bean.MediaBase
import com.example.glidedemo.bean.MediaData
import com.example.glidedemo.databinding.ActivityPhotoDetailBinding
import com.example.glidedemo.databinding.FlowlayoutTextBinding
import com.example.glidedemo.views.flowlayout.FlowLayout
import com.example.glidedemo.views.flowlayout.TagAdapter
import com.example.glidedemo.views.flowlayout.TagFlowLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import java.util.ArrayList

class PhotoDetailActivity : BaseActivity(), TagFlowLayout.OnTagClickListener {


    private val mVals by lazy {
        arrayOf("flow", "第一个Subsampling", "中间普通加载", "第三个是gesture")
    }


    private val binding by lazy {
        ActivityPhotoDetailBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initFlowLayout()
        val receivedList: ArrayList<MediaBase> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra("detail_list_data", MediaBase::class.java)
                    ?: ArrayList()
            } else {
                intent.getSerializableExtra("scan_list") as? ArrayList<MediaBase> ?: ArrayList()
            }

        Log.d("223366", "onCreate: ------: ${receivedList.size}")
        if (receivedList.isNotEmpty()) {
            val photo = receivedList.getOrNull(1) as MediaData
            val uri = Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, (photo.id).toString()
            )
            try {
                binding.detailPhotoSubImageView.setImage(ImageSource.uri(uri))
            } catch (e: Exception) {
                Log.e("223366", "error: ---detailPhotoSubImageView-:${e.message}")
            }

            try {
                Glide.with(this).load(photo.path).into(binding.detailPhotoImageView)
            } catch (e: Exception) {
                Log.e("223366", "error: ---detailPhotoSubImageView-:${e.message}")
            }
            try {
//                Glide.with(this).load(photo.path).into(binding.detailPhotoGestureImageView)
            } catch (e: Exception) {
                Log.e("223366", "error: ---detailPhotoSubImageView-:${e.message}")
            }
        }

    }

    private fun initFlowLayout() {
        binding.tagFlowLayout.apply {
            addAdapter(object : TagAdapter<Any>(mVals.toList()) {
                override fun getView(parent: FlowLayout?, position: Int, s: Any): View {
                    val tvBinding =
                        FlowlayoutTextBinding.inflate(layoutInflater, binding.tagFlowLayout, false)
                    tvBinding.root.text = s.toString()
                    return tvBinding.root
                }
            })
        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun main() = runBlocking {
        val flow1 = flow {
            emit(1)
            kotlinx.coroutines.delay(100)
            emit(2)
        }

        val flow2 = flow {
            emit(3)
            kotlinx.coroutines.delay(50)
            emit(4)
        }

        // 创建一个包含多个 Flow 的流
        val flowOfFlows = flowOf(flow1, flow2)

        // 使用 flattenMerge 合并这些 Flow
        flowOfFlows.flattenMerge().collect { value ->
            println(value)
        }

    }

    override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
        when (position) {
            0 -> {
                main()
            }

            else -> {}
        }
        return true
    }


}
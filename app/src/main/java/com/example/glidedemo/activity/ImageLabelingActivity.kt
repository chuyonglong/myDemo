package com.example.glidedemo.activity

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.glidedemo.bean.Medium
import com.example.glidedemo.databinding.ActivityImageLabelingBinding
import com.example.glidedemo.extensions.TAG
import com.example.glidedemo.extensions.loadImage
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.utils.MediaFileQuery
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageLabelingActivity : AppCompatActivity() {

    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    private val binding by viewBindings(ActivityImageLabelingBinding::inflate)
    private var mediaList = arrayListOf<Medium>()
    private var currentIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActions()
        // 异步加载媒体文件
        lifecycleScope.launch(Dispatchers.IO) {
            mediaList = MediaFileQuery.queryAllData(this@ImageLabelingActivity)
        }
    }

    private fun initActions() {
        binding.buttonClassify.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                if (mediaList.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        toast("没有数据!")
                    }
                    return@launch
                }

                // 获取当前图片
                val media = mediaList[currentIndex].apply {
                    currentIndex = (currentIndex + 1) % mediaList.size
                }

                withContext(Dispatchers.Main) {
                    binding.classifyImageView.loadImage(media.path)
                }
                getBitmapFromUrl(this@ImageLabelingActivity, media.path).let { bitmap ->
                    if (bitmap == null) {
                        withContext(Dispatchers.Main) {
                            toast("加载图片失败!")
                        }
                        return@let
                    }
                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    // TODO: 调用 MLKit 进行图像分类
                    labeler.process(inputImage)
                        .addOnSuccessListener { labels ->
                            Log.d(TAG, "initActions: labels: $labels")
                            var resultText = ""
                            for ((index, label) in labels.withIndex()) {
                                val text = label.text
                                val confidence = label.confidence
                                resultText += "[$index] $text: $confidence\n"
                            }
                            lifecycleScope.launch(Dispatchers.Main) {
                                binding.textViewResults.text = resultText.trim()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "initActions: e: $e")
                        }
                }


            }
        }
    }

    private suspend fun getBitmapFromUrl(context: Context, imageUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .submit()
                    .get()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


}
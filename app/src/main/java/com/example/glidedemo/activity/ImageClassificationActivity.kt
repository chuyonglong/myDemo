package com.example.glidedemo.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.bean.Medium
import com.example.glidedemo.databinding.ActivityImageClassificationBinding
import com.example.glidedemo.extensions.loadImage
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.utils.ImageClassifierHelper
import com.example.glidedemo.utils.MediaFileQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.task.vision.classifier.Classifications

class ImageClassificationActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private val binding by viewBindings(ActivityImageClassificationBinding::inflate)
    private var mediaList = arrayListOf<Medium>()
    private var currentIndex: Int = 0



    private val imageClassifierHelper: ImageClassifierHelper by lazy {
//        ImageClassifierHelper(context = this,currentModel =1, imageClassifierListener = this)
        ImageClassifierHelper(
            threshold = 0.5f,
            numThreads = 2,
            maxResults = 3,
            currentDelegate = 0,
            currentModel = 5,
            context = this,
            imageClassifierListener = this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                // 加载 Bitmap 图像
                val bitmap = imageClassifierHelper.getSampleBitmap(media.path) ?: run {
                    withContext(Dispatchers.Main) {
                        toast("图像加载失败！")
                    }
                    return@launch
                }
                // 执行图像分类
                classifyImage(bitmap)
            }
        }

        // 异步加载媒体文件
        lifecycleScope.launch(Dispatchers.IO) {
            mediaList = MediaFileQuery.queryAllData(this@ImageClassificationActivity)
        }
    }




    private fun classifyImage(image: Bitmap) {
        // Copy out RGB bits to the shared bitmap buffer

        // Pass Bitmap and rotation to the image classifier helper for processing and classification
        imageClassifierHelper.classify(image)
    }

    override fun onDestroy() {
        super.onDestroy()
        imageClassifierHelper.clearImageClassifier()
    }


    override fun onError(error: String) {
        Log.d("223366", "onError: error")
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (results == null) {
                binding.textViewResults.text = "识别失败！"
            } else {
                var str = "识别结果：\n"
                for ((index, result) in results.withIndex()) {
                    str += if (result.categories.isEmpty()) {
                        "${index + 1}. 没有识别到任何内容\n"
                    } else {
                        "${index + 1}. ${result.categories[0].label} 准确率${result.categories.first().score * 100}\n"
                    }
                }
                binding.textViewResults.text = str
            }
        }

    }
}
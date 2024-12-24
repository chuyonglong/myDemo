package com.example.glidedemo.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.bean.Medium
import com.example.glidedemo.databinding.ActivityImageClassificationBinding
import com.example.glidedemo.extensions.loadImage
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.utils.MediaFileQuery
import com.example.glidedemo.utils.TFLiteHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageClassificationInterpreterActivity : AppCompatActivity() {

    private val binding by viewBindings(ActivityImageClassificationBinding::inflate)

    private var mediaList = arrayListOf<Medium>()
    private var currentIndex: Int = 0

    // 设置图像相关的常量
    private val IMAGE_SIZE_X = 224
    private val IMAGE_SIZE_Y = 224
    private val DIM_PIXEL_SIZE = 3 // RGB 通道数量
    private val NUM_CLASSES = 1000 // 根据你的模型设置分类类别数量

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 加载 TFLite 模型
        try {
            TFLiteHelper.loadModelFromAssets("efficientnet-lite0.tflite")
        } catch (e: Exception) {
            toast("模型加载失败：${e.message}")
            return
        }
        val inputTensor = TFLiteHelper.interpreter?.getInputTensor(0)
        val inputTensorType = inputTensor?.dataType()
        val inputTensorShape = inputTensor?.shape()
        Log.d(
            "ModelInfo",
            "Input tensor type: $inputTensorType, shape: ${inputTensorShape?.contentToString()}"
        )


        // 点击按钮触发图像分类
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
                val bitmap = getSampleBitmap(media) ?: run {
                    withContext(Dispatchers.Main) {
                        toast("图像加载失败！")
                    }
                    return@launch
                }

                // 执行图像分类
                val results = try {
                    classifyImage(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }

                // 显示分类结果
                results?.let {
                    displayResults(it[0])
                }
            }
        }

        // 异步加载媒体文件
        lifecycleScope.launch(Dispatchers.IO) {
            mediaList =
                MediaFileQuery.queryAllData(this@ImageClassificationInterpreterActivity)
        }
    }

    /**
     * 图像分类函数
     * @param bitmap 输入图像
     * @return 分类结果（每个类别的置信度）
     */

    private fun classifyImage(bitmap: Bitmap): Array<ByteArray> {
        if (bitmap.width != IMAGE_SIZE_X || bitmap.height != IMAGE_SIZE_Y) {
            throw IllegalArgumentException("输入的图像尺寸必须为 ${IMAGE_SIZE_X}x${IMAGE_SIZE_Y}")
        }

        // 分配 uint8 的输入缓冲区
        val inputBuffer =
            ByteBuffer.allocateDirect(IMAGE_SIZE_X * IMAGE_SIZE_Y * DIM_PIXEL_SIZE)
        inputBuffer.order(ByteOrder.nativeOrder())

        // 将 Bitmap 转换为 UINT8 数据
        for (y in 0 until IMAGE_SIZE_Y) {
            for (x in 0 until IMAGE_SIZE_X) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF // 提取红色通道
                val g = (pixel shr 8) and 0xFF  // 提取绿色通道
                val b = pixel and 0xFF          // 提取蓝色通道
                inputBuffer.put(r.toByte())    // 添加红色通道值
                inputBuffer.put(g.toByte())    // 添加绿色通道值
                inputBuffer.put(b.toByte())    // 添加蓝色通道值
            }
        }

        inputBuffer.rewind()


        // 创建一个适配模型输出的 ByteArray 类型的结果
        val output = Array(1) { ByteArray(NUM_CLASSES) }

        try {
            // 执行推理，使用 UINT8 输入
            TFLiteHelper.interpreter?.run(inputBuffer, output)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ImageClassification", "Error during inference: ${e.message}")
        }
        return output
    }


    /**
     * 显示分类结果
     * @param results 分类结果（ByteArray）
     */
    private suspend fun displayResults(output: ByteArray) {
        val resultText = StringBuilder()
        // 假设你想将每个输出的值转换为百分比并显示
        val scaleFactor = 255.0 // 如果输出为 0-255 的 UINT8
        output.forEachIndexed { index, value ->
            val probability = value.toFloat() / scaleFactor
            if (probability > 0.00) {
                resultText.append("Class $index: ${String.format("%.2f", probability * 100)}%\n")
            }

        }
        if(resultText.isEmpty()){
            resultText.append("没有匹配的")
        }

        // 更新 UI
        withContext(Dispatchers.Main) {
            binding.textViewResults.text = resultText.toString().trim()
        }
    }


    /**
     * 获取 Bitmap 图像
     */
    private fun getSampleBitmap(media: Medium): Bitmap? {
        val path = media.path
        return try {
            BitmapFactory.decodeFile(path)?.let {
                Bitmap.createScaledBitmap(it, IMAGE_SIZE_X, IMAGE_SIZE_Y, true).also { bitmap ->
                    requireNotNull(bitmap) { "图像文件为空" }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放模型资源
        TFLiteHelper.closeInterpreter()
    }



}

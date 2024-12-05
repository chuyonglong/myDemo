package com.example.glidedemo.utils

import android.content.res.AssetFileDescriptor
import android.util.Log
import android.widget.Toast
import com.example.glidedemo.App
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object TFLiteHelper {
      var interpreter: Interpreter? = null
    private var expectedOutputSize: Int = 0 // 存储模型输出大小

    /**
     * 加载模型（从 assets 文件夹加载）
     * @param modelPath 模型文件路径（例如 "model.tflite"）
     */
    fun loadModelFromAssets(modelPath: String) {
        try {
            val tfLiteModel = loadModelFile(modelPath)
            val options = Interpreter.Options().apply {
                setNumThreads(4) // 设置线程数，根据设备性能调整
            }
            interpreter = Interpreter(tfLiteModel, options)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 从 assets 加载 .tflite 模型
     * @param modelPath 模型文件路径
     * @return 映射的字节缓冲区
     */
    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = App.getContext().assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * 运行推理
     * @param input 输入数据（ByteBuffer 格式）
     * @return 推理结果（FloatArray 格式）
     */
    fun runInference(input: ByteBuffer): FloatArray {
        requireNotNull(interpreter) {
            Toast.makeText(
                App.getContext(),
                "Interpreter has not been initialized. Call loadModelFromAssets() first.",
                Toast.LENGTH_SHORT
            ).show()
        }

//        val inputTensor = interpreter!!.getInputTensor(0)
//        val inputShape = inputTensor.shape()
//        val expectedInputSize = inputShape.reduce { acc, i -> acc * i } * 4 // 每个 float 有 4 字节

        // 获取输出张量的大小并计算 expectedOutputSize
        val outputTensor = interpreter!!.getOutputTensor(0)
        val outputShape = outputTensor.shape() // 获取输出形状
        expectedOutputSize = outputShape.reduce { acc, i -> acc * i } * 4 // 每个 float 有 4 字节

        // 确保 input 的大小正确
//        if (input.remaining() != expectedInputSize) {
//            throw IllegalArgumentException(
//                "Input ByteBuffer has incorrect size: " +
//                        "Expected $expectedInputSize bytes but got ${input.remaining()} bytes."
//            )
//        }

        val output = FloatArray(expectedOutputSize) // 创建输出数组
        Log.d("TFLiteHelper", "runInference: $input  ------")
        interpreter!!.run(input, output)
        return output
    }

    /**
     * 关闭 Interpreter 以释放资源
     */
    fun closeInterpreter() {
        interpreter?.close()
        interpreter = null
    }

    /**
     * 创建输入的 ByteBuffer
     * @return 正确大小的 ByteBuffer
     */
    fun createInputBuffer(data: FloatArray): ByteBuffer {
        val expectedInputSize = 150528 // 根据你的模型输入大小设置
        val inputBuffer =
            ByteBuffer.allocateDirect(expectedInputSize).order(ByteOrder.nativeOrder())
        inputBuffer.asFloatBuffer().put(data) // 填充数据
        return inputBuffer
    }
}

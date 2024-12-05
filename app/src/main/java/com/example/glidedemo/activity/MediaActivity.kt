package com.example.glidedemo.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.glidedemo.adapter.MediaAdapter
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.bean.MediaData
import com.example.glidedemo.databinding.ActivityMediaBinding
import com.example.glidedemo.entity.AppData
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.permission.GalleryPermissionUtils
import com.example.glidedemo.utils.ImageClassifierHelper
import com.example.glidedemo.utils.MediaQueryForPermission
import com.example.glidedemo.utils.beGone
import com.example.glidedemo.utils.beVisible
import com.example.glidedemo.views.GalleryGridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.task.vision.classifier.Classifications

class MediaActivity : BaseActivity(), ImageClassifierHelper.ClassifierListener,
    MediaAdapter.OnMediaItemClickListener {

    private val binding by lazy {
        ActivityMediaBinding.inflate(layoutInflater)
    }

    private val mediaAdapter by lazy {
        MediaAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initActions()
        initAdapter()
    }

    private var applyCount = 0

    override fun onResume() {
        super.onResume()
        val permissionEnum = GalleryPermissionUtils.checkMediaPermissionResult(this)
        if (applyCount < 2) {
            if (permissionEnum == GalleryPermissionUtils.PermissionEnum.NO_PERMISSIONS) {
                GalleryPermissionUtils.requestMediaPermissions(mediaPermissionLauncher)
                applyCount++
            } else if (permissionEnum == GalleryPermissionUtils.PermissionEnum.PARTIAL_PERMISSIONS) {
                binding.permissionView.beGone()
                binding.mediaView.beVisible()
                binding.requestPermission.beVisible()
                showData()
            } else {
                binding.permissionView.beGone()
                binding.mediaView.beVisible()
                showData()
            }
        } else {
            checkPermissionAndTryGoHome()
        }
    }

    /**
     * 权限回调
     */
    private val mediaPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.isNotEmpty()) {
                checkPermissionAndTryGoHome()
            }
        }

    /**
     * 检查权限尝试进入主页
     */
    private fun checkPermissionAndTryGoHome() {
        val permissionEnum = GalleryPermissionUtils.checkMediaPermissionResult(this)
        if (permissionEnum == GalleryPermissionUtils.PermissionEnum.NO_PERMISSIONS) {
            binding.permissionButton.setOnClickListener {
                if (applyCount >= 2) {
                    openAppSetting()
                } else {
                    GalleryPermissionUtils.requestMediaPermissions(mediaPermissionLauncher)
                    applyCount++
                }
            }
            applyCount++
        } else if (permissionEnum == GalleryPermissionUtils.PermissionEnum.PARTIAL_PERMISSIONS) {
            binding.permissionView.beGone()
            binding.mediaView.beVisible()
            binding.requestPermission.beVisible()
            showData()
        } else {
            binding.permissionView.beGone()
            binding.mediaView.beVisible()
            showData()
        }
    }


    private fun showData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = MediaQueryForPermission.queryAllData(this@MediaActivity)
            withContext(Dispatchers.Main) {
                mediaAdapter.setMediaList(list)
            }
        }
    }


    private fun initAdapter() {
        binding.mediaGrid.adapter = mediaAdapter
        val layoutManager = GalleryGridLayoutManager(
            this, 4, LinearLayoutManager.VERTICAL, false
        )
        binding.mediaGrid.layoutManager = layoutManager

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (mediaAdapter.isASectionTitle(position)) {
                    layoutManager.spanCount
                } else {
                    1
                }
            }
        }
        mediaAdapter.setMediaItemClickListener(this)
    }


    private fun initActions() {
        binding.requestPermission.setOnClickListener {
            GalleryPermissionUtils.requestMediaPermissions(mediaPermissionLauncher)
        }
    }


    private fun openAppSetting() {
        val mIntent = Intent()
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        mIntent.data = Uri.fromParts("package", this.packageName, null)
        this.startActivity(mIntent)
    }


    private val imageClassifierHelper: ImageClassifierHelper by lazy {
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


    override fun onError(error: String) {
        Log.d("223366", "onError: error")
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (results == null) {
                toast("识别失败！", Toast.LENGTH_LONG)
            } else {
                var str = "识别结果：\n"
                for ((index, result) in results.withIndex()) {
                    str += if (result.categories.isEmpty()) {
                        "${index + 1}. 没有识别到任何内容\n"
                    } else {
                        "${index + 1}. ${result.categories[0].label} 准确率${result.categories.first().score * 100}\n"
                    }
                }
                toast(str, Toast.LENGTH_LONG)
            }
        }

    }

    override fun mediaClick(item: MediaData) {
        // 加载 Bitmap 图像
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = imageClassifierHelper.getSampleBitmap(item.path) ?: run {
                withContext(Dispatchers.Main) {
                    toast("图像加载失败！")
                }
                return@launch
            }
            // 执行图像分类
            imageClassifierHelper.classify(bitmap)
        }
    }

}
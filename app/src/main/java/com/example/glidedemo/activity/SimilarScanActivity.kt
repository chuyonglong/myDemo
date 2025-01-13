package com.example.glidedemo.activity

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.R
import com.example.glidedemo.bean.CleanUpDetailData
import com.example.glidedemo.bean.CleanUpScanData
import com.example.glidedemo.bean.ScanMediaData
import com.example.glidedemo.databinding.ActivityCleanupSacnningBinding
import com.example.glidedemo.dialog.CleanUpPauseDialog
import com.example.glidedemo.extensions.byte2FitMemorySizeToString
import com.example.glidedemo.extensions.onDemoBackPressed
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.permission.GalleryPermissionUtils
import com.example.glidedemo.utils.CleanTypeEnum
import com.example.glidedemo.utils.CleanupFunctionUtils
import com.example.glidedemo.utils.DataQueueManager
import com.example.glidedemo.utils.DialogClickEnum
import com.example.glidedemo.utils.MediaTypeEnum
import com.example.glidedemo.utils.QueryStateEnum
import com.example.glidedemo.utils.ScanState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SimilarScanActivity : AppCompatActivity(), CleanupFunctionUtils.ItemCallback {

    private val binding by viewBindings(ActivityCleanupSacnningBinding::inflate)

    private var scanDataList = arrayListOf<CleanUpScanData>()
    private val cleanupFunctionUtils = CleanupFunctionUtils()
    private var scanJob: Job? = null
    private var toolbarShown = false
    private var queryStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initialize()
    }

    private fun initialize() {
        cleanupFunctionUtils.reSetQuery()
        startScreenshotScan()
        startSimilarPhotoScan()
        observeScanState()

        onDemoBackPressed(true) {
            cleanupFunctionUtils.stopQuery()
        }
    }

    /**
     * 扫描截图
     */
    private fun startScreenshotScan() = lifecycleScope.launch {
        val screenshotData = scanScreenshots()
        screenshotData?.let { scanDataList.add(it) }
    }

    private suspend fun scanScreenshots(): CleanUpScanData? = withContext(Dispatchers.IO) {
        val screenshotPaths = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        )
        var totalSize = 0L
        var fileCount = 0
        val screenshotFiles = arrayListOf<ScanMediaData>()

        screenshotPaths.forEach { path ->
            val screenshotFolder = File(path, "Screenshots")
            if (screenshotFolder.exists()) {
                screenshotFolder.listFiles()?.filter { it.isFile }?.forEach { file ->
                    if (screenshotFiles.size < 5) {
                        screenshotFiles.add(
                            ScanMediaData(
                                file.name, file.path, file.length(),
                                file.lastModified(), MediaTypeEnum.TYPE_IMAGES
                            )
                        )
                    }
                    fileCount++
                    totalSize += file.length()
                }
            }
        }

        return@withContext if (fileCount > 0) {
            val sizeStr = totalSize.byte2FitMemorySizeToString()
            CleanUpScanData(
                CleanTypeEnum.TYPE_SCREENSHOT,
                getString(R.string.screenshot),
                getString(R.string.occupied, sizeStr),
                getString(R.string.scan_screenshot, fileCount),
                fileCount, totalSize, screenshotFiles
            )
        } else null
    }

    /**
     * 扫描相似图片
     */
    private fun startSimilarPhotoScan() {
        scanJob?.cancel()
        lifecycleScope.launch {
            val permissionEnum =
                GalleryPermissionUtils.checkMediaPermissionResult(this@SimilarScanActivity)
            if (permissionEnum != GalleryPermissionUtils.PermissionEnum.NO_PERMISSIONS) {
                startScanAnimation()
                cleanupFunctionUtils.setItemCallback(this@SimilarScanActivity)
                scanJob = lifecycleScope.launch(Dispatchers.IO) {
                    cleanupFunctionUtils.querySimilarData(this@SimilarScanActivity)
                }
            }
        }
    }

    private fun startScanAnimation() {
        queryStartTime = SystemClock.elapsedRealtime()
        binding.laScanLottie.playAnimation()
    }

    /**
     * 扫描结果回调
     */
    override suspend fun itemCallback(item: CleanUpDetailData?, state: QueryStateEnum) {
        when (state) {
            QueryStateEnum.FINISH -> finishScan(item)
            QueryStateEnum.PAUSE -> handlePauseState()
            else -> DataQueueManager.addSimilarSource(item)
        }
        updateToolbarVisibility()
    }

    private suspend fun finishScan(item: CleanUpDetailData?) {
        cleanupFunctionUtils.clearCursor()
        DataQueueManager.addSimilarSource(item)
    }

    private fun handlePauseState() = lifecycleScope.launch {
        binding.laScanLottie.cancelAnimation()
        CleanUpPauseDialog(this@SimilarScanActivity) { result ->
            when (result) {
                DialogClickEnum.OK -> lifecycleScope.launch { DataQueueManager.addSimilarSource(null) }
                else -> {
                    startScanAnimation()
                    lifecycleScope.launch(Dispatchers.IO) {
                        cleanupFunctionUtils.reSetQuery()
                        cleanupFunctionUtils.querySimilarData(this@SimilarScanActivity)
                    }
                }
            }
        }
    }

    private suspend fun updateToolbarVisibility() {
        if (SystemClock.elapsedRealtime() - queryStartTime > 5000 && !toolbarShown) {
            withContext(Dispatchers.Main) {
                binding.homeCleanupToolbar.apply {
                    setNavigationIcon(R.drawable.ic_go_back)
                    setNavigationOnClickListener { cleanupFunctionUtils.stopQuery() }
                    toolbarShown = true
                }
            }
        }
    }

    /**
     * 监听扫描状态
     */
    private fun observeScanState() = lifecycleScope.launch {
        DataQueueManager.scanState.collectLatest {
            when (it) {
                is ScanState.Loading -> binding.tvScanningText.text = getString(R.string.scanning, it.count)
                is ScanState.Complete -> handleScanComplete(it)
            }
        }
    }

    private suspend fun handleScanComplete(state: ScanState.Complete) {
        if (state.similarCount > 0) {
            val sizeStr = state.size.byte2FitMemorySizeToString()
            val similarData = CleanUpScanData(
                CleanTypeEnum.TYPE_SIMILAR,
                getString(R.string.similar_photos),
                getString(R.string.occupied, sizeStr),
                getString(R.string.scan_similar, state.similarCount),
                state.similarCount, state.size, state.list
            )
            scanDataList.add(0, similarData)
        }

        val delayTime = 2000 - (SystemClock.elapsedRealtime() - queryStartTime)
        if (delayTime > 0) delay(delayTime)

        Intent(this@SimilarScanActivity, SimilarActivity::class.java).apply {
            putExtra("scan_list", scanDataList)
            startActivity(this)
            finish()
        }
        binding.laScanLottie.cancelAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanJob?.cancel()
        DataQueueManager.clear()
        binding.laScanLottie.cancelAnimation()
    }
}

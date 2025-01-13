package com.example.glidedemo.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.glidedemo.R
import com.example.glidedemo.adapter.CleanUpMainAdapter
import com.example.glidedemo.bean.CleanUpScanData
import com.example.glidedemo.databinding.ActivityCleanupMainBinding
import com.example.glidedemo.extensions.byte2FitMemorySizeToString
import com.example.glidedemo.extensions.byte2FitMemorySizeToStringAndUnit
import com.example.glidedemo.utils.DataQueueManager
import com.example.glidedemo.utils.MMKVUtils
import com.example.glidedemo.utils.ScanState
import com.example.glidedemo.utils.beGone
import com.example.glidedemo.utils.beVisible
import com.example.glidedemo.views.GalleryLinearLayoutManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SimilarActivity : AppCompatActivity(), CleanUpMainAdapter.OnItemClickListener {
    private val binding: ActivityCleanupMainBinding by lazy {
        ActivityCleanupMainBinding.inflate(layoutInflater)
    }

    //查询数据的任务
    private var job: Job? = null

    //扫描分组adapter
    private var similarScanAdapter = CleanUpMainAdapter()

    //扫描分组数据
    private var scanDataList: ArrayList<CleanUpScanData> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        DataQueueManager.clearCount()
        scanDataList = intent.getSerializableExtra("scan_list") as ArrayList<CleanUpScanData>
        if (scanDataList.size > 0) {
            setupAdapter()
            similarScanAdapter.setSimilarScanList(scanDataList)
            getTotalSize().byte2FitMemorySizeToStringAndUnit { size, unit ->
                binding.tvSize.text = size
                binding.tvUnit.text = unit
                val cleanUpSize = size + "_" + unit
                MMKVUtils.cleanupSize = cleanUpSize
            }
            initObserve()
        } else {
            getTotalSize().byte2FitMemorySizeToStringAndUnit { size, unit ->
                val cleanUpSize = size + "_" + unit
                MMKVUtils.cleanupSize = cleanUpSize
            }
            binding.cleanupFiles.beGone()
            binding.noCleanupFiles.beVisible()
            binding.tvFinishButton.setOnClickListener {
                job?.cancel()
                doFinish()
            }
        }
        binding.scanningToolbar.apply {
            setNavigationOnClickListener {
                job?.cancel()
                doFinish()
            }
        }

    }

    override fun onResume() {
        super.onResume()
    }

    private fun initObserve() = lifecycleScope.launch {
        launch {
            DataQueueManager.scanState.collectLatest {
                if (it is ScanState.Delete) {
                    val oldCleanScanData = scanDataList.find { data ->
                        data.type == it.type
                    }
                    val sizeStr = it.size.byte2FitMemorySizeToString()
                    if (oldCleanScanData != null) {
                        MMKVUtils.similarCount = it.count.toString()
                        oldCleanScanData.sizeStr = getString(R.string.occupied, sizeStr)
                        oldCleanScanData.size = it.size
                        oldCleanScanData.content = getString(R.string.scan_similar, it.count)
                        oldCleanScanData.count = it.count
                        oldCleanScanData.pathList.clear()
                        oldCleanScanData.pathList.addAll(it.list)

                    }
                    getTotalSize().byte2FitMemorySizeToStringAndUnit { size, unit ->
                        binding.tvSize.text = size
                        binding.tvUnit.text = unit
                        val cleanUpSize = size + "_" + unit
                        MMKVUtils.cleanupSize = cleanUpSize
                    }
                    similarScanAdapter.setSimilarScanList(scanDataList)
                }
            }
        }
    }

    /**
     * 比较是否相似
     */
    private fun setupAdapter() {
        similarScanAdapter.setOnItemClickListener(this)
        val currAdapter = binding.rvSimilar.adapter
        if (currAdapter == null) {
            binding.rvSimilar.adapter = similarScanAdapter
            binding.rvSimilar.layoutManager =
                GalleryLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onItemClick(item: CleanUpScanData) {
        DataQueueManager.setSimilarDataSize(item.size, item.count)
        val type = item.type
        val intent = Intent(this, CleanUpDetailActivity::class.java)
        intent.putExtra("type", type.name)
        startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        //清除单例数据
        DataQueueManager.clear()
    }


    private fun getTotalSize(): Long {
        var totalSize = 0L
        scanDataList.forEach {
            totalSize += it.size
        }
        return totalSize
    }

    private fun doFinish() {
        Intent(this@SimilarActivity, MainActivity::class.java).apply {
            startActivity(this)
            this@SimilarActivity.finish()
        }
    }


}
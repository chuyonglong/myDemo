package com.example.glidedemo.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.glidedemo.adapter.MediaAdapter
import com.example.glidedemo.adapter.MediaListAdapter
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.databinding.ActivityMediaBinding
import com.example.glidedemo.permission.GalleryPermissionUtils
import com.example.glidedemo.utils.MediaQueryForPermission
import com.example.glidedemo.utils.beGone
import com.example.glidedemo.utils.beVisible
import com.example.glidedemo.views.GalleryGridLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaListActivity : BaseActivity() {

    private val binding by lazy {
        ActivityMediaBinding.inflate(layoutInflater)
    }

    private val mediaListAdapter by lazy {
        MediaListAdapter()
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
            val list = MediaQueryForPermission.queryAllMediaListData(this@MediaListActivity)
            withContext(Dispatchers.Main) {
                mediaListAdapter.submitList(list)
            }
        }
    }


    private fun initAdapter() {
        binding.mediaGrid.adapter = mediaListAdapter
        val layoutManager = GalleryGridLayoutManager(
            this, 4, LinearLayoutManager.VERTICAL, false
        )
        binding.mediaGrid.layoutManager = layoutManager

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (mediaListAdapter.isASectionTitle(position)) {
                    layoutManager.spanCount
                } else {
                    1
                }
            }
        }
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

}
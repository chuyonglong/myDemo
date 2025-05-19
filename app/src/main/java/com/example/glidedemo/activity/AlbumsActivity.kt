package com.example.glidedemo.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.bean.MediaBase
import com.example.glidedemo.bean.MediaData
import com.example.glidedemo.databinding.ActivityAlbumsBinding
import com.example.glidedemo.databinding.FlowlayoutTextBinding
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.permission.GalleryPermissionUtils
import com.example.glidedemo.utils.MediaQueryForPermission
import com.example.glidedemo.utils.PermissionUtil
import com.example.glidedemo.views.flowlayout.FlowLayout
import com.example.glidedemo.views.flowlayout.TagAdapter
import com.example.glidedemo.views.flowlayout.TagFlowLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumsActivity : AppCompatActivity(), TagFlowLayout.OnTagClickListener,
    TagFlowLayout.OnSelectListener {

    private val binding by viewBindings(ActivityAlbumsBinding::inflate)

    private val mVals by lazy {
        linkedMapOf(
            "0" to "0:相机",
            "1" to "1:相机",
            "2" to "2:list adapter ",
            "3" to "3:media list adapter",
            "4" to "4:临时权限",
            "5" to "5:全屏显示图片",
            "6" to "6:后台拍照无预览相机",
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initFlowLayout()
    }

    private fun initFlowLayout() {
        binding.tagFlowLayout.apply {
            addAdapter(object : TagAdapter<Any>(mVals.values.toList()) {
                override fun getView(parent: FlowLayout?, position: Int, s: Any): View {
                    val tvBinding =
                        FlowlayoutTextBinding.inflate(layoutInflater, binding.tagFlowLayout, false)
                    tvBinding.root.text = s.toString()
                    return tvBinding.root
                }
            })
        }
        binding.tagFlowLayout.apply {
            setOnTagClickListener(this@AlbumsActivity)
            setOnSelectListener(this@AlbumsActivity)
        }
    }

    override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
        when (position) {
            0 -> {
                // 请求相机权限
                val hasCameraPermission =
                    PermissionUtil.hasPermissions(this, Manifest.permission.CAMERA)
                if (hasCameraPermission) {
                    openCamera()
                } else {
                    requestCameraPermission.launch(Manifest.permission.CAMERA)
                }
            }

            1 -> {
                startActivity(Intent(this, MediaActivity::class.java))
            }

            2 -> {
                startActivity(Intent(this, ListAdapterActivity::class.java))
            }

            3 -> {
                startActivity(Intent(this, MediaListActivity::class.java))
            }

            4 -> {
                toast("如点击没反应,检查媒体权限,申请权限点击 '媒体' 按钮")
                // :    这里的问题是之前 MediaStore.Images.Media.EXTERNAL_CONTENT_URI 选错了
                //                val path = "/storage/emulated/0/svg/Cat.svg"
                lifecycleScope.launch(Dispatchers.IO) {
                    val list: MutableList<MediaBase> =
                        MediaQueryForPermission.queryAllData(this@AlbumsActivity)
                    list.getOrNull(0)?.let {
                        withContext(Dispatchers.Main) {
                            val uri = Uri.withAppendedPath(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                ((it as MediaData).id).toString()
                            )
                            val writeRequest = MediaStore.createWriteRequest(
                                contentResolver, mutableListOf<Uri>(uri)
                            ).intentSender
                            moveVaultLauncher.launch(
                                IntentSenderRequest.Builder(writeRequest).build()
                            )
                        }
                    }

                }
            }

            5 -> {
                val permissionEnum = GalleryPermissionUtils.checkMediaPermissionResult(this)
                if (permissionEnum == GalleryPermissionUtils.PermissionEnum.NO_PERMISSIONS) {
                    toast("没有权限----,请求权限!!!")
                    GalleryPermissionUtils.requestMediaPermissions(mediaPermissionLauncher)
                } else {
                    goPhotoDetailActivity()
                }
            }

            6 -> {
                startActivity(Intent(this, BackgroundCameraActivity::class.java))
            }


        }
        return true
    }

    override fun onSelected(selectPosSet: Set<Int>?) {}


    /**
     * 相机权限
     */
    private val requestCameraPermission: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            // 权限被拒绝，显示提示信息
            toast("相机权限被拒绝")
        }
    }

    /**
     * 拍照Launcher
     */
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        }

    /**
     * 打开相机
     */
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }

    private val moveVaultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                toast("请求权限成功")
            }
        }

    /**
     * 媒体权限回调
     */
    private val mediaPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.isNotEmpty()) {
                goPhotoDetailActivity()
            }
        }

    /**
     * 获取 数据跳转PhotoDetailActivity
     */

    private fun goPhotoDetailActivity() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list: MutableList<MediaBase> =
                MediaQueryForPermission.queryAllData(this@AlbumsActivity)

            if (list.isEmpty()) {
                withContext(Dispatchers.Main) {
                    toast("没有数据,别点了!!!")
                }
                return@launch
            }

            val finalLt: ArrayList<MediaBase> = if (list.size < 10) {
                ArrayList(list)
            } else {
                ArrayList(list.subList(0, 10))
            }
            Intent(this@AlbumsActivity, PhotoDetailActivity::class.java).apply {
                putExtra("detail_list_data", finalLt)
                startActivity(this)
            }
        }
    }

}
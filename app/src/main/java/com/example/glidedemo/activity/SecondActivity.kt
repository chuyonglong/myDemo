package com.example.glidedemo.activity

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_MEDIA_LOCATION
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.example.glidedemo.R
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.bean.Medium
import com.example.glidedemo.databinding.ActivitySecondBinding
import com.example.glidedemo.utils.MediaFileQuery
import java.util.concurrent.atomic.AtomicBoolean


class SecondActivity : BaseActivity() {

    private val binding: ActivitySecondBinding by lazy {
        ActivitySecondBinding.inflate(layoutInflater)
    }
    private var oneIShow = AtomicBoolean(false)


    @get:Synchronized
    private var twoIShow = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btClickOne.setOnClickListener {
            if (!oneIShow.getAndSet(true)) {
                oneClickOne()
            } else {
                oneIShow.set(false)
                binding.ivImageOne.setImageBitmap(null)
            }

        }
        binding.btClickTwo.setOnClickListener {
            if (!twoIShow) {
                twoIShow = true
                oneClickTwo()
            } else {
                binding.ivImageTwo.setImageBitmap(null)
                twoIShow = false
            }

        }
        binding.btGetLocation.setOnClickListener {
            requestPermissionsForLocation()
        }

        binding.apply {
            btGoBack.setOnClickListener {
                val intent = Intent().apply {
                    putExtras(Bundle().apply {
                        putString("text2", "aaaaaa")
                    })
                }
                setResult(Activity.RESULT_OK,intent)
                finish()
            }
            getPermission.setOnClickListener {
                requestPermissionsForMedia()
            }

            getMedia.setOnClickListener {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        this@SecondActivity,
                        ACCESS_MEDIA_LOCATION
                    ) == PERMISSION_GRANTED
                    if (!hasPermission) {
                        Log.d("223366", "onCreate:--------没图片位置权限 ")
                        ActivityCompat.requestPermissions(
                            this@SecondActivity,
                            arrayOf(ACCESS_MEDIA_LOCATION),
                            0
                        )
                    }
                }
                val hasReadExternalStorage = ContextCompat.checkSelfPermission(
                    this@SecondActivity,
                    READ_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED
                if (!hasReadExternalStorage) {
                    Log.d("223366", "onCreate:--------没所有文件权限 ")
                    ActivityCompat.requestPermissions(
                        this@SecondActivity,
                        arrayOf(READ_EXTERNAL_STORAGE),
                        0
                    )
                } else {
                    Log.d("223366", "onCreate:--------有所有文件权限 ")
                }


                val list = MediaFileQuery.queryAllData(this@SecondActivity)
                val list1 = list.filter { it.path.contains("/storage/emulated/0/DCIM/Camera/PXL") }
                    .map { it } as ArrayList<Medium>
                Log.d("223366", "onCreate: ----(${list1.size})")
                val path = list1[0].path
                Log.d("223366", "onCreate: ----($path)")
                val exif: ExifInterface = getExifInterface(path)

                val weidu = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)//指示纬度。
                val wei = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)//指示纬度是北纬还是南纬。
                val versionId =
                    exif.getAttribute(ExifInterface.TAG_GPS_VERSION_ID)//指示 GPS Info IFD 的版本。
                val jing = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)// 指示经度是东经还是西经。
                val datetime =
                    exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP)// 将时间指示为 UTC（协调世界时）。
                val jingdu = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)// 指示经度。
                val xietiaodate =
                    exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP)// 记录相对于 UTC（协调世界时）的日期和时间信息的字符串。
                val orientation =
                    exif.getAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION)// 指示捕获图像时图像的方向。
                val cankao =
                    exif.getAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION_REF)// 指示在捕获图像时给出图像方向的参考。
                Log.d("223366", "onCreate: 纬-------($wei)")
                Log.d("223366", "onCreate: 纬度-------($weidu)")
                Log.d("223366", "onCreate: 纬度-------------(${coordinateTransformation(weidu)})")
                Log.d("223366", "onCreate: 经-------($jing)")
                Log.d("223366", "onCreate: 经度-------($jingdu)")
                Log.d("223366", "onCreate: 经度-------------(${coordinateTransformation(jingdu)})")

                val latLon = FloatArray(2)
                if (exif.getLatLong(latLon)) {
                    Log.d("223366", "onCreate: 经纬度-------(${latLon[0]}, ${latLon[1]})")
                }

                binding.textShow.text = "${latLon[0]}, ${latLon[1]},\n 文件:${path}"
            }
        }

        binding.goNext.setOnClickListener {
            startActivity(Intent(this, ThreeActivity::class.java))
        }

        intent.extras?.apply {
            val textShow = this.getString("text1")
            binding.textShow.text = textShow

        }

    }

    /**
     * 将 112/1,58/1,390971/10000 格式的经纬度转换成 112.99434397362694格式
     * @param string 度分秒
     * @return 度
     */
    private fun coordinateTransformation(string: String?): Double {
        var dimensionality = 0.0
        if (null == string) {
            return dimensionality
        }

        //用 ，将数值分成3份
        val split = string.split(",".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        for (i in split.indices) {
            val s = split[i].split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            //用112/1得到度分秒数值
            val v = s[0].toDouble() / s[1].toDouble()
            //将分秒分别除以60和3600得到度，并将度分秒相加
            dimensionality = dimensionality + v / Math.pow(60.0, i.toDouble())
        }
        return dimensionality
    }

    private fun getExifInterface(path: String): ExifInterface {
//        applicationContext.contentResolver.openInputStream(uri)
//        ExifInterface((this).getFileInputStreamSync(path)!!)
        return ExifInterface(path)


    }

    override fun onResume() {
        super.onResume()
        Log.d("223366", "onResume: ")
//        checkPermissionResult()
    }


    private fun oneClickOne() {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(resources, R.drawable.ic_image_load, options)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        val imageType = options.outMimeType

        Log.d("223366", "onCreate:-----imageHeight-----$imageHeight ")
        Log.d("223366", "onCreate:-----imageWidth-----$imageWidth")

        binding.ivImageOne.post {
            binding.ivImageOne.setImageBitmap(
                decodeSampledBitmapFromResource(
                    resources,
                    R.drawable.ic_image_load,
                    binding.ivImageOne.width, binding.ivImageOne.height
                )
            )
        }

    }

    private fun oneClickTwo() {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(resources, R.drawable.ic_image_load, options)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        val imageType = options.outMimeType

        Log.d("223366", "onCreate:-----imageHeight-----$imageHeight ")
        Log.d("223366", "onCreate:-----imageWidth-----$imageWidth")

        binding.ivImageTwo.post {
            binding.ivImageTwo.setImageBitmap(
                decodeSampledBitmapFromResource(
                    resources,
                    R.drawable.ic_image_load,
                    binding.ivImageTwo.width, binding.ivImageTwo.height
                )
            )
        }


    }


    private fun decodeSampledBitmapFromResource(
        res: Resources?, resId: Int, reqWidth: Int, reqHeight: Int
    ): Bitmap {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)
        // 调用上面定义的方法计算inSampleSize值

        options.inSampleSize =
            calculateInSampleSize(options, reqWidth, reqHeight)
        Log.d("223366", "decodeSampledBitmapFromResource: ----${options.inSampleSize}")
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeResource(res, resId, options)
        return bitmap
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // 源图片的高度和宽度
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    private fun requestPermissionsForMedia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPermissions(
                arrayOf(
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VIDEO,
                    READ_MEDIA_VISUAL_USER_SELECTED
                ), 0
            )
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO), 0)
//            permissionLauncher.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO))
        } else {
            requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), 0)
//            permissionLauncher.launch(arrayOf(READ_EXTERNAL_STORAGE))
        }
    }

    private val locationForMedia =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ACCESS_MEDIA_LOCATION else ""

    private val location = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)


    private fun requestPermissionsForLocation() {
        requestPermissions(location, 0)

    }


    private fun checkPermissionResult() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && (ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES) == PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                this,
                READ_MEDIA_VIDEO
            ) == PERMISSION_GRANTED)
        ) {
            // Android 13及以上完整照片和视频访问权限
            Log.d("223366", "checkPermissionResult:-------Android 13及以上完整照片和视频访问权限 ")

        } else if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            ContextCompat.checkSelfPermission(
                this,
                READ_MEDIA_VISUAL_USER_SELECTED
            ) == PERMISSION_GRANTED
        ) {
            binding.getPermissionOne.visibility = View.VISIBLE
            binding.getPermissionOne.setOnClickListener {
                requestPermissionsForMedia()
            }
            //
            Log.d("223366", "checkPermissionResult:-------Android 14及以上部分照片和视频访问权限 ")
        } else if (ContextCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) == PERMISSION_GRANTED
        ) {
            // Android 12及以下完整本地读写访问权限
            Log.d("223366", "checkPermissionResult:-------12及以下完整本地读写访问权限 ")
        } else {
            // 无本地读写访问权限
            Log.d("223366", "checkPermissionResult:-------无本地读写访问权限 ")
        }
    }


}
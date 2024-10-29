package com.example.glidedemo.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.AvailabilityCallback
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Camera2BackgroundUtil1(activity: Activity) {
    private val context: Context = activity

    //    private CameraCallBack cameraCallBack;
    private val cameraManager: CameraManager?

    // 默认相机id是0 LENS_FACING_FRONT,LENS_FACING_BACK
    private val cameraId = CameraCharacteristics.LENS_FACING_BACK
    private var mCameraDevice: CameraDevice? = null
    private var savePath: String? = null

    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
    private var mCameraCaptureSession: CameraCaptureSession? = null
    private var request: CaptureRequest? = null
    private var service: ExecutorService? = null
    private var isTakedPicture = false //是否已经拍照

    private var needSetOrientation = 0 // 设置默认的拍照方向
    private var isSessionClosed = true // captureSession是否被关闭
    private var isCameraDoing = false // 是否正在使用相机
    private val CAPTURE_DELAY_TIME_LONG: Long = 1200 // 延时拍照——聚焦需要时间

    private val HANDLER_ERR = 3 // 拍照失败
    private val HANDLER_TAKE_PHOTO_SUCCESS = 5 // 拍照成功
    private var enableCameraList: MutableList<Int> = mutableListOf() //可用摄像头列表
    private var mFlashSupported = false //是否支持闪光灯
    private var recordSizeList: MutableList<Size> = mutableListOf() // 录制尺寸
    private val mPreviewOutputSize: Size? = null // 预览尺寸
    private var imgOutputSizes: MutableList<Size> = mutableListOf() // 拍照尺寸

    private val PREVIEW_TYPE_NORMAL = 0 // 默认预览
    private val PREVIEW_TYPE_RECORD = 1 // 录屏预览
    private val PREVIEW_TYPE_TAKE_PHOTO = 2 // 拍照预览
    private var previewType = 0 //默认预览

    private var lastSaveFileTime: Long = 0

    /**
     * 处理静态图片的输出
     */
    private var imageReader: ImageReader? = null

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                HANDLER_ERR -> //                    cameraCallBack.onErr("" + msg.obj);
                    isCameraDoing = false

                HANDLER_TAKE_PHOTO_SUCCESS -> {
                    //                    cameraCallBack.onTakePhotoOk(savePath);
                    val paths = arrayOf(savePath)
                    MediaScannerConnection.scanFile(context, paths, null) { path, uri -> }
                }
            }
        }
    }

    /**
     * 当相机设备的状态发生改变的时候，将会回调。
     */
    protected val stateCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {
            /**
             * 当相机打开的时候，调用
             * @param cameraDevice
             */
            override fun onOpened(cameraDevice: CameraDevice) {
                Log.d("223366", "onOpened")
                mCameraDevice = cameraDevice
                startPreview()
            }

            override fun onDisconnected(cameraDevice: CameraDevice) {
                Log.d("223366", "onDisconnected")
                cameraDevice.close()
                mCameraDevice = null
                val message = Message()
                message.what = HANDLER_ERR
                message.obj = "后台相机断开连接"
                handler.sendMessage(message)
            }

            /**
             * 发生异常的时候调用
             *
             * 这里释放资源，然后关闭界面
             * @param cameraDevice
             * @param error
             */
            override fun onError(cameraDevice: CameraDevice, error: Int) {
                Log.d("223366", "onError 相机设备异常,请重启！")
                cameraDevice.close()
                mCameraDevice = null
                val messagef = Message()
                messagef.what = HANDLER_ERR
                messagef.obj = "相机设备异常,请重启！"
                handler.sendMessage(messagef)
            }

            /**
             * 当相机被关闭的时候
             */
            override fun onClosed(camera: CameraDevice) {
                super.onClosed(camera)
                Log.d("223366", "onClosed")
                mCameraDevice = null
                isCameraDoing = false
            }
        }

    /**
     * 相机状态回调
     */
    private val callback: AvailabilityCallback = object : AvailabilityCallback() {
        override fun onCameraAvailable(cameraId: String) { // 相机可用
            super.onCameraAvailable(cameraId)
            Log.d("223366", "相机可用")
        }

        override fun onCameraUnavailable(cameraId: String) { // 相机不可用
            super.onCameraUnavailable(cameraId)
            Log.d("223366", "相机不可用")
        }
    }


    private fun setupImageReader() {
        //前三个参数分别是需要的尺寸和格式，最后一个参数代表每次最多获取几帧数据，本例的3代表ImageReader中最多可以获取2帧图像流
        imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1)
        //监听ImageReader的事件，当有图像流数据可用时会回调onImageAvailable方法，它的参数就是预览帧数据，可以对这帧数据进行处理
        imageReader!!.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            //我们可以将这帧数据转成字节数组，类似于Camera1的PreviewCallback回调的预览帧数据
            val buffer = image.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer[data]
            image.close()
            saveFile(data, savePath)
        }, null)
    }

    /**
     * 打开相机
     */
    @SuppressLint("MissingPermission")
    private fun openCamera() {
        Log.d("223366", "openCamera:$cameraId")
        isCameraDoing = true

        // 设置TextureView的缓冲区大小
        // 获取Surface显示预览数据
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val message = Message()
            message.what = HANDLER_ERR
            message.obj = "权限不足"
            handler.sendMessage(message)
            return
        }
        try {
            cameraManager!!.openCamera(cameraId.toString(), stateCallback, null)
            Log.d("223366", "打开相机成功！")
        } catch (e: Exception) {
            Log.e("223366", "打开相机失败-Exception:" + e.message)
            e.printStackTrace()
            val messagef = Message()
            messagef.what = HANDLER_ERR
            messagef.obj = "打开相机失败"
            handler.sendMessage(messagef)
        }
    }


    /**
     * 开始视频录制预览
     */
    private fun startPreview() {
        Log.d("223366", "startPreview")
        // CaptureRequest添加imageReaderSurface，不加的话就会导致ImageReader的onImageAvailable()方法不会回调
        // 创建CaptureSession时加上imageReaderSurface，如下，这样预览数据就会同时输出到previewSurface和imageReaderSurface了
        try {
            // 创建CaptureRequestBuilder，TEMPLATE_PREVIEW比表示预览请求
            mPreviewRequestBuilder =
                mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            mPreviewRequestBuilder!!.addTarget(imageReader!!.surface) // 设置Surface作为预览数据的显示界面
            //设置拍照方向
            mPreviewRequestBuilder!!.set(CaptureRequest.JPEG_ORIENTATION, needSetOrientation)
            // 创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            mCameraDevice!!.createCaptureSession(
                Arrays.asList(
                    imageReader!!.surface
                ), captureSessionStateCallBack, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            val messagef = Message()
            messagef.what = HANDLER_ERR
            messagef.obj = "捕获帧失败"
            handler.sendMessage(messagef)
            Log.e("223366", "Camera获取成功，创建录制请求或捕获Session失败" + e.message, e)
        }
    }

    /**
     * 捕获图片数据
     */
    private val captureSessionStateCallBack: CameraCaptureSession.StateCallback =
        object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                try {
                    mCameraCaptureSession = session
                    isSessionClosed = false
                    request = mPreviewRequestBuilder!!.build()
                    // 设置反复捕获数据的请求，这样预览界面就会一直有数据显示
                    mCameraCaptureSession!!.setRepeatingRequest(request!!, null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                    val messagef = Message()
                    messagef.what = HANDLER_ERR
                    messagef.obj = "开启图像预览失败"
                    handler.sendMessage(messagef)
                }
                if (!isTakedPicture) {
                    isTakedPicture = true
                    handler.postDelayed({ takePicture() }, CAPTURE_DELAY_TIME_LONG)
                }
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.d("223366", "onConfigureFailed")
            }
        }


    /**
     * 初始化
     *
     * @param activity
     */
    init {
        cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        mySetDisplayOrientation(cameraId)
        // 对于静态图片，使用可用的最大值来拍摄。
        cameraManager.registerAvailabilityCallback(callback, null)
//            cameraInfo
        setupImageReader()
        service = Executors.newSingleThreadExecutor()
    }

    fun startTakePicture(savePath: String) {
        Log.d("223366", "拍照：$savePath")
        this.savePath = savePath
        if (isCameraDoing) {
            Log.d("223366", "相机使用中...")
        } else {
            isTakedPicture = false
            openCamera()
        }
    }

    /**
     * 拍照
     */
    private fun takePicture() {
        Log.d("223366", "takePicture")
        try {
            if (mCameraDevice == null || mPreviewRequestBuilder == null) {
                return
            }
            mPreviewRequestBuilder!!.addTarget(imageReader!!.surface)
            Log.d(
                "223366", "takePicture: needSetOrientation-needSetOrientation:$needSetOrientation"
            )
            Log.d(
                "223366",
                "takePicture: mPreviewRequestBuilder-mPreviewRequestBuilder:" + (mPreviewRequestBuilder == null)
            )
            //这个回调接口用于拍照结束时重启预览，因为拍照会导致预览停止
            val mImageSavedCallback: CaptureCallback = object : CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    Log.d("223366", "拍照完成")
                    onStop()
                }
            }
            //开始拍照，然后回调上面的接口重启预览，因为mCaptureBuilder设置ImageReader作为target，所以会自动回调ImageReader的onImageAvailable()方法保存图片
            mCameraCaptureSession!!.capture(
                mPreviewRequestBuilder!!.build(), mImageSavedCallback, null
            )
        } catch (e: CameraAccessException) {
            Log.d("223366", "takePhoto CameraAccessException:" + e.message)
            e.printStackTrace()
            val messagef = Message()
            messagef.what = HANDLER_ERR
            messagef.obj = "拍照失败"
            handler.sendMessage(messagef)
        }
    }

    /**
     * 停止预览，释放资源
     */
    fun stopRecord() {
        Log.d("223366", "停止预览，释放资源")
        try {
            if (mCameraCaptureSession != null && !isSessionClosed) {
                mCameraCaptureSession!!.stopRepeating()
                mCameraCaptureSession!!.abortCaptures()
                mCameraCaptureSession!!.close()
                isSessionClosed = true
            }
            if (mCameraDevice != null) {
                mCameraDevice!!.close()
            }
            isCameraDoing = false
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("223366", "stopRecord-Exception:" + e.message, e)
        }
    }

    /**
     * 重置后，开始预览
     */
    fun reset() {
        previewType = PREVIEW_TYPE_NORMAL
        stopRecord()
        openCamera()
    }

    /**
     * 在 activity,fragment的onStop中调用
     */
    fun onStop() {
        stopRecord()
    }

    /**
     * 注销 回调
     */
    fun onDestroy() {
        cameraManager?.unregisterAvailabilityCallback(callback)
    }

    /**
     * 获得可用的摄像头
     *
     * @return SparseArray of available cameras ids。key为摄像头方位，见CameraCharacteristics#LENS_FACING，value为对应的摄像头id
     */
    private val cameras: Unit
        get() {
            if (cameraManager == null) {
                return
            }
            enableCameraList = ArrayList()
            try {
                val camerasAvailable = cameraManager.cameraIdList
                var cam: CameraCharacteristics
                var characteristic: Int
                Log.d("223366", "-------------------------------------")
                for (id in camerasAvailable) {
                    Log.d("223366", "getCameras:$id")
                    try {
                        enableCameraList.add(id.toInt())
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                }
                Log.d("223366", "-------------------------------------")
            } catch (e: CameraAccessException) {
                Log.e("223366", "getCameras CameraAccessException:" + e.message, e)
            }
        }

    val outputSizeSelector: Unit
        /**
         * 设置输出数据尺寸选择器，在selectCamera之前设置有效
         * （一般手机支持多种输出尺寸，请用户根据自身需要选择最合适的一种）
         * 举例：
         * SizeSelector maxPreview = SizeSelectors.and(SizeSelectors.maxWidth(720), SizeSelectors.maxHeight(480));
         * SizeSelector minPreview = SizeSelectors.and(SizeSelectors.minWidth(320), SizeSelectors.minHeight(240));
         * camera.setmOutputSizeSelector(SizeSelectors.or(
         * SizeSelectors.and(maxPreview, minPreview)//先在最大和最小中寻找
         * , SizeSelectors.and(maxPreview, SizeSelectors.biggest())//找不到则按不超过最大尺寸的那个选择
         * ));
         */
        get() {
        }


    private val cameraInfo: Unit
        /**
         * 获取摄像头信息
         */
        get() {
            if (enableCameraList.isEmpty()) {
                cameras
            }
            try {
                val mCameraCharacteristics =
                    cameraManager!!.getCameraCharacteristics(cameraId.toString())
                // 设置是否支持闪光灯
                val available =
                    mCameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                mFlashSupported = available ?: false
                val map =
                    mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                if (map == null) {
                    Log.e("223366", "Could not get configuration map.")
                    return
                }
                val mSensorOrientation =
                    mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!! //这个方法来获取CameraSensor的方向。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Log.d(
                        "223366",
                        "camera sensor orientation:$mSensorOrientation,display rotation=" + context.display!!.rotation
                    )
                }

                val formats = map.outputFormats //获得手机支持的输出格式，其中jpeg是一定会支持的，yuv_420_888是api21才开始支持的
                for (format in formats) {
                    Log.d("223366", "手机格式支持: $format")
                }
                val yuvOutputSizes = map.getOutputSizes(ImageFormat.YUV_420_888)
                val mediaOutputSizes = map.getOutputSizes(
                    MediaRecorder::class.java
                )
                val previewOutputSizes = map.getOutputSizes(
                    SurfaceTexture::class.java
                )
                val jpegOutputSizes = map.getOutputSizes(ImageFormat.JPEG)


                Log.d("223366", "---------------------------------------------------")
                for (size in mediaOutputSizes) {
                    recordSizeList.add(Size(size.width, size.height))
                    Log.d("223366", "mediaOutputSizes: $size")
                }
                for (size in jpegOutputSizes) {
                    imgOutputSizes.add(Size(size.width, size.height))
                    Log.d("223366", "jpegOutputSizes: $size")
                }
                for (size in previewOutputSizes) {
                    Log.d("223366", "previewOutputSizes: $size")
                }
                for (size in yuvOutputSizes) {
                    Log.d("223366", "yuvOutputSizes: $size")
                }
                Log.d("223366", "---------------------------------------------------")
            } catch (e: Exception) {
                Log.e("223366", "selectCamera Exception:" + e.message, e)
            }
        }

    //覆盖性保存
    private fun saveFile(data: ByteArray?, savePath: String?) {
        if (data == null || data.size == 0) {
            return
        }
        if (System.currentTimeMillis() - lastSaveFileTime > 1000) {
            service!!.execute {
                val file = File(savePath)
                val parent = file.parentFile
                if (parent != null && !parent.exists()) {
                    parent.mkdirs()
                }
                if (file.exists()) {
                    file.delete()
                }
                try {
                    file.createNewFile()
                    val fos = FileOutputStream(file)
                    fos.write(data)
                    fos.flush()
                    fos.close()
                    lastSaveFileTime = System.currentTimeMillis()
                    val message = Message()
                    message.what = HANDLER_TAKE_PHOTO_SUCCESS
                    message.obj = savePath
                    handler.sendMessage(message)
                } catch (e: IOException) {
                    e.printStackTrace()
                    val messagef = Message()
                    messagef.what = HANDLER_ERR
                    messagef.obj = "图片保存失败"
                    handler.sendMessage(messagef)
                }
            }
        }
    }

    /**
     * 设置摄像头旋转方向
     */
    private fun mySetDisplayOrientation(cameraId: Int) { // 调整摄像头方向
        val mCameraCharacteristics =
            cameraManager!!.getCameraCharacteristics(cameraId.toString())
        val sensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        var degrees = 0
        when (sensorOrientation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        // back-facing
        val result = (sensorOrientation!! - degrees + 360) % 360
        needSetOrientation = result
    }
}


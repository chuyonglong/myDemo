package com.example.glidedemo.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
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
import android.media.MediaScannerConnection
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Surface
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Camera2BackgroundUtil2(activity: Activity) {
    private val context: Context = activity
    private val cameraManager: CameraManager by lazy {
        activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    // 默认相机id是0 LENS_FACING_FRONT,LENS_FACING_BACK
    private val cameraId = CameraCharacteristics.LENS_FACING_BACK
    private var mCameraDevice: CameraDevice? = null
    private var savePath: String? = null

    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
    private var mCameraCaptureSession: CameraCaptureSession? = null
    private var request: CaptureRequest? = null
    private var service: ExecutorService? = null

    //是否已经拍照
    private var isTakedPicture = false

    // 设置默认的拍照方向
    private var needSetOrientation = 0

    // captureSession是否被关闭
    private var isSessionClosed = true

    // 是否正在使用相机
    private var isCameraDoing = false

    // 延时拍照——聚焦需要时间
    private val CAPTURE_DELAY_TIME_LONG: Long = 1200

    // 拍照失败
    private val HANDLER_ERR = 3

    // 拍照成功
    private val HANDLER_TAKE_PHOTO_SUCCESS = 5

    // 默认预览
    private val PREVIEW_TYPE_NORMAL = 0

    //默认预览
    private var previewType = 0

    private var lastSaveFileTime: Long = 0

    /**
     * 处理静态图片的输出
     */
    private var imageReader: ImageReader? = null

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                HANDLER_ERR -> {
                    isCameraDoing = false
                }

                HANDLER_TAKE_PHOTO_SUCCESS -> {
                    val paths = arrayOf(savePath)
                    MediaScannerConnection.scanFile(context, paths, null) { _, _ -> }
                }
            }
        }
    }

    /**
     * 当相机设备的状态发生改变的时候，将会回调。
     */
    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        /**
         * 当相机打开的时候，调用
         * @param cameraDevice
         */
        override fun onOpened(cameraDevice: CameraDevice) {
            mCameraDevice = cameraDevice
            startPreview()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraDevice.close()
            mCameraDevice = null
        }

        /**
         * 发生异常的时候调用
         *
         * 这里释放资源，然后关闭界面
         * @param cameraDevice
         * @param error
         */
        override fun onError(cameraDevice: CameraDevice, error: Int) {
            cameraDevice.close()
            mCameraDevice = null
        }

        /**
         * 当相机被关闭的时候
         */
        override fun onClosed(camera: CameraDevice) {
            super.onClosed(camera)
            mCameraDevice = null
            isCameraDoing = false
        }
    }

    /**
     * 相机状态回调
     */
    private val callback: AvailabilityCallback = object : AvailabilityCallback() {

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
            savePath?.let { saveFile(data, it) }
        }, null)
    }

    /**
     * 打开相机
     */
    @SuppressLint("MissingPermission")
    private fun openCamera() {
        isCameraDoing = true
        // 设置TextureView的缓冲区大小
        // 获取Surface显示预览数据
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        try {
            cameraManager.openCamera(cameraId.toString(), stateCallback, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 开始视频录制预览
     */
    private fun startPreview() {
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
                }
                if (!isTakedPicture) {
                    isTakedPicture = true
                    handler.postDelayed({ takePicture() }, CAPTURE_DELAY_TIME_LONG)
                }
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
            }
        }


    /**
     * 初始化
     */
    init {
        mySetDisplayOrientation(cameraId)
        // 对于静态图片，使用可用的最大值来拍摄。
        cameraManager.registerAvailabilityCallback(callback, null)
        setupImageReader()
        service = Executors.newSingleThreadExecutor()
    }

    /**
     * 对外方法
     * 拍照
     */
    fun startTakePicture(savePath: String) {
        this.savePath = savePath
        if (isCameraDoing) {
            return
        } else {
            isTakedPicture = false
            openCamera()
        }
    }

    /**
     * 拍照
     */
    private fun takePicture() {
        try {
            if (mCameraDevice == null || mPreviewRequestBuilder == null) {
                return
            }
            mPreviewRequestBuilder!!.addTarget(imageReader!!.surface)

            //这个回调接口用于拍照结束时重启预览，因为拍照会导致预览停止
            val mImageSavedCallback: CaptureCallback = object : CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    onStop()
                }
            }
            //开始拍照，然后回调上面的接口重启预览，因为mCaptureBuilder设置ImageReader作为target，所以会自动回调ImageReader的onImageAvailable()方法保存图片
            mCameraCaptureSession!!.capture(
                mPreviewRequestBuilder!!.build(), mImageSavedCallback, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * 停止预览，释放资源
     */
    fun stopRecord() {
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
        cameraManager.unregisterAvailabilityCallback(callback)
    }

    //覆盖性保存
    private fun saveFile(data: ByteArray?, savePath: String) {
        if (data == null || data.isEmpty()) {
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
                }
            }
        }
    }

    /**
     * 设置摄像头旋转方向
     */
    private fun mySetDisplayOrientation(cameraId: Int) { // 调整摄像头方向
        val mCameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId.toString())
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


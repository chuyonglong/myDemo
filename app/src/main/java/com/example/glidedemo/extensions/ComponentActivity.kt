package com.example.glidedemo.extensions

import android.content.Context
import android.graphics.Color
import android.media.MediaScannerConnection
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding


fun <T : ViewBinding> ComponentActivity.viewBindings(
    inflater: (LayoutInflater) -> T, statusBar: SystemBarStyle = SystemBarStyle.light(
        Color.TRANSPARENT, Color.TRANSPARENT
    )
): Lazy<T> {
    return lazy {
        val viewBinding = inflater.invoke(layoutInflater)
        enableEdgeToEdge(statusBar)
        setContentView(viewBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewBinding
    }
}

/**
 * 实现完全沉浸式
 */
fun ComponentActivity.hideSystemBars(type: Int) {
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    windowInsetsController.hide(type)
}


fun AppCompatActivity.onLockBackPressed(isEnabled: Boolean, callback: () -> Unit) {
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(isEnabled) {
        override fun handleOnBackPressed() {
            callback()
        }
    })


}

fun notifyGallery(context: Context, path: String) {
    MediaScannerConnection.scanFile(
        context, arrayOf(path), null
    ) { _, _ ->
    }
}
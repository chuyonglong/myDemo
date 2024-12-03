package com.example.glidedemo.extensions

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

private var toast: Toast? = null

fun Context.toast(res: Int, duration: Int = Toast.LENGTH_SHORT) {
    toast(getString(res), duration)
}

fun Context.toast(str: String, duration: Int = Toast.LENGTH_SHORT) {
    MainScope().launch {
        toast?.cancel()
        toast = Toast.makeText(applicationContext, str, duration)
        toast?.show()
    }
}

fun Fragment.toast(res: Int, duration: Int = Toast.LENGTH_SHORT) {
    context?.toast(res, duration)

}

fun Fragment.toast(str: String, duration: Int = Toast.LENGTH_SHORT) {
    context?.toast(str, duration)
}


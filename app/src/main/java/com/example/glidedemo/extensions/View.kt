package com.example.glidedemo.extensions

import android.app.Activity
import android.view.View
import com.binioter.guideview.Component
import com.binioter.guideview.GuideBuilder

fun View.showGuideView(
    activity: Activity, round: Int, component: Component, dismissCallback: () -> Unit
) {
    val build = GuideBuilder()
    build.setTargetView(this).setAlpha(150).setHighTargetCorner(round).addComponent(component)
    build.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
        override fun onShown() {
        }

        override fun onDismiss() {
            dismissCallback()
        }
    })
    build.createGuide().show(activity)
}
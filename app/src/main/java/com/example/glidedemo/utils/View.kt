package com.example.glidedemo.utils

import android.view.View

fun View.beInvisible() {
    visibility = View.INVISIBLE
}

fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beGone() {
    visibility = View.GONE
}

fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()

fun View.beInVisibleIf(beInVisible: Boolean) = if (beInVisible) beInvisible() else beVisible()

fun View.beGoneIf(beGone: Boolean) = if (beGone) beGone() else beVisible()

fun View.isVisible() = visibility == View.VISIBLE

fun View.isGone() = visibility == View.GONE

fun View.isInVisibleIf() = visibility == View.INVISIBLE


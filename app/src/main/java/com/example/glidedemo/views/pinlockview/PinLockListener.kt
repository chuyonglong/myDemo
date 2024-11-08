package com.example.glidedemo.views.pinlockview


interface PinLockListener {
    fun onComplete(pin: String)
    fun onEmpty()
    fun onPinChange(pinLength: Int, intermediatePin: String?)
}

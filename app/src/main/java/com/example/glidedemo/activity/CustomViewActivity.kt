package com.example.glidedemo.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.glidedemo.R
import com.example.glidedemo.databinding.ActivityCustomViewBinding
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.views.StackSliderView

class CustomViewActivity : AppCompatActivity() {


    private val binding by viewBindings(ActivityCustomViewBinding::inflate)
    private val images = mutableListOf<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        loadImages()
        initData()
    }

    private fun initData() {

        binding.stackSliderView.setAdapter(object : StackSliderView.Adapter {
            override fun getCount(): Int = images.size

            override fun loadImage(imageView: ImageView, position: Int) {
                Glide.with(imageView).load(images[position]).into(imageView)
            }
        })
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadImages() {
//        val drawable1 = getDrawable(R.drawable.img_notification_clean)
//        val drawable2 = getDrawable(R.drawable.ic_lite_photo)
//        val drawable3 = getDrawable(R.drawable.ic_lite_photo)
        val drawable1 = R.drawable.img_notification_clean
        val drawable2 = R.drawable.ic_lite_photo
        val drawable3 = R.drawable.ic_lite_photo
        images.add(drawable1)
        images.add(drawable2)
        images.add(drawable3)


    }


}

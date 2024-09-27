package com.example.glidedemo.activity

import android.os.Bundle
import com.example.glidedemo.R
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.databinding.ActivityLikeBinding


class LikeActivity : BaseActivity() {

    private val mVals by lazy {
        arrayOf(
            "点赞", "喜欢", "笑脸", "收藏",
        )
    }
    private val mVals1 by lazy {
        arrayOf(
            R.raw.heart,
            R.raw.like,
            R.raw.smile,
            R.raw.star,
        )
    }


    private val binding by lazy {
        ActivityLikeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }


}
package com.example.glidedemo.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.glidedemo.R

fun ImageView.loadImage(path: String) {
    val options = RequestOptions()
        .skipMemoryCache(false)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .optionalTransform(CenterCrop())
//            optionalTransform(
//                WebpDrawable::class.java, WebpDrawableTransformation(CenterCrop())
//            )
        .dontAnimate()

    Glide.with(this).load(path)
        .placeholder(R.drawable.ic_image_loading)
        .apply(options)
        .error(R.drawable.ic_image_load_error)
        .into(this)

}
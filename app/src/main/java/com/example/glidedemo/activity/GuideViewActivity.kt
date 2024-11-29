package com.example.glidedemo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.binioter.guideview.GuideBuilder
import com.example.glidedemo.databinding.ActivityGuideViewBinding
import com.example.glidedemo.views.SimpleComponent


class GuideViewActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityGuideViewBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        showGuide()
    }


   private fun showGuide(){
       binding.imageGuide.post {
           val build = GuideBuilder()
           build.setTargetView(binding.imageGuide)
               .setAlpha(50)
               .setHighTargetCorner(20)
               .setHighTargetPadding(10)
               .addComponent(SimpleComponent())
           build.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
               override fun onShown() {

               }

               override fun onDismiss() {
                   // TODO: 展示第二个
               }

           })
           build.createGuide().show(this)
       }
    }


}
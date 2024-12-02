package com.example.glidedemo.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.glidedemo.adapter.DemoViewPagerStateAdapter
import com.example.glidedemo.databinding.ActivityTabLayoutBinding
import com.example.glidedemo.databinding.ViewTabLayoutHomesBinding
import com.example.glidedemo.extensions.viewBindings
import com.google.android.material.tabs.TabLayoutMediator

class TabLayoutActivity : AppCompatActivity() {

    private val binding by viewBindings(ActivityTabLayoutBinding::inflate)


    private val demoViewPagerStateAdapter by lazy {
        DemoViewPagerStateAdapter(this)
    }
    private val tabs by lazy {
        arrayListOf("张三", "李四","王五111")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initViewPager()
    }

    private fun initViewPager() {
        binding.appLockListViewPager.adapter = demoViewPagerStateAdapter
        binding.appLockListTabLayout.clearOnTabSelectedListeners()
        binding.appLockListViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {


                    else -> {}
                }
            }

        })
        val tabLayoutMediator = TabLayoutMediator(
            binding.appLockListTabLayout, binding.appLockListViewPager
        ) { tab, position ->
            tab.customView = getBarViewAtI(position, tabs)
        }
        tabLayoutMediator.attach()
    }

    private fun getBarViewAtI(position: Int, tabs: ArrayList<String>): View {
        val tabLayoutBinding = ViewTabLayoutHomesBinding.inflate(layoutInflater, null, false)
        tabLayoutBinding.homeTabText.text = tabs[position]
        return tabLayoutBinding.root
    }


}
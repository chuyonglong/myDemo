package com.example.glidedemo.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.glidedemo.fragment.AppLockFragment
import com.example.glidedemo.fragment.AppUnLockFragment

class DemoViewPagerStateAdapter(context: FragmentActivity) : FragmentStateAdapter(context) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AppUnLockFragment()
            1 -> AppLockFragment()
            2 -> AppLockFragment()
            else -> error("")
        }
    }

}
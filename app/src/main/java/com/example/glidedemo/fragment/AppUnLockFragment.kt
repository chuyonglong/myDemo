package com.example.glidedemo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.glidedemo.adapter.LockListAdapter
import com.example.glidedemo.databinding.FragmentAppUnLockBinding
import com.example.glidedemo.entity.AppData
import com.example.glidedemo.utils.beGone
import com.example.glidedemo.utils.beVisible
import com.example.glidedemo.utils.beVisibleIf

class AppUnLockFragment : Fragment(), LockListAdapter.OnGuideItemClickListener {

    private var binding: FragmentAppUnLockBinding? = null


    private val lockListAdapter by lazy {
        LockListAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return FragmentAppUnLockBinding.inflate(inflater).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initAdapter()
        initData()
    }

    private fun initData() {
        binding?.noAppLayout?.beVisible()
        binding?.appUnLockListGrid?.beGone()
    }

    private fun initAdapter() {

    }

    /**
     * 产品说保留切换页面的状态所以注释
     * 2024年11月20日
     */
//    override fun onPause() {
//        super.onPause()
//        lockListAdapter.selectAll(false)
//    }

    override fun guideClick(item: AppData) {

    }

    override fun guideSelectAll(isSelect: Boolean) {

    }


}
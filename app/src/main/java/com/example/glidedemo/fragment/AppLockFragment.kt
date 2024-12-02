package com.example.glidedemo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.glidedemo.adapter.LockListAdapter
import com.example.glidedemo.databinding.FragmentAppLockBinding
import com.example.glidedemo.entity.AppData
import com.example.glidedemo.utils.beGone
import com.example.glidedemo.utils.beVisible
import com.example.glidedemo.views.GalleryGridLayoutManager

class AppLockFragment : Fragment(), LockListAdapter.OnGuideItemClickListener {

    private var binding: FragmentAppLockBinding? = null

    private val lockListAdapter by lazy {
        LockListAdapter()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return FragmentAppLockBinding.inflate(inflater).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initAdapter()
        initData()
    }

    private fun initData() {
        binding?.noAppLayout?.beVisible()
        binding?.appLockListGrid?.beGone()
    }

    private fun initAdapter() {
        binding?.let {
            it.appLockListGrid.adapter = lockListAdapter
            it.appLockListGrid.layoutManager = GalleryGridLayoutManager(requireActivity(), 3)
        }
        lockListAdapter.setGuideItemClickListener(this)

    }

    override fun guideClick(item: AppData) {

    }

    override fun guideSelectAll(isSelect: Boolean) {

    }

    /**
     * 产品说保留切换页面的状态所以注释
     * 2024年11月20日
     */
//    override fun onPause() {
//        super.onPause()
//        lockListAdapter.selectAll(true)
//        appLockListViewModel.removeAll()
//    }


}
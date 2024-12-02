package com.example.glidedemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.glidedemo.databinding.ItemGuideBinding
import com.example.glidedemo.entity.AppData

class LockListAdapter : RecyclerView.Adapter<LockListAdapter.GuideViewHolder>() {
    private var allDataList = mutableListOf<AppData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        return GuideViewHolder(
            ItemGuideBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount() = allDataList.size

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val item = allDataList[position]
        holder.bindItem(item)
    }


    inner class GuideViewHolder(val binding: ItemGuideBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var appData: AppData? = null

        init {
            binding.root.setOnClickListener {
                appData?.let {
                    it.isLocked = !it.isLocked
                    notifyItemChanged(absoluteAdapterPosition)
                    guideItemClickListener?.guideClick(it)
                }
            }
        }

        fun bindItem(item: AppData) {
            appData = item
            binding.root.isSelected = item.isLocked
            binding.guideAppIcon.setImageDrawable(item.iconDrawable)
            binding.guideAppName.text = item.appName
        }
    }


    /**
     *
     */
    @SuppressLint("NotifyDataSetChanged")
    fun selectAll(isSelect: Boolean) {
        for (item in allDataList) {
            item.isLocked = isSelect

        }
        guideItemClickListener?.guideSelectAll(isSelect)
        notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun addAllDataList(list: List<AppData>) {
        allDataList.clear()
        allDataList.addAll(list.map { it.copyWithIconDrawable(lockState = it.isLocked) })
        notifyDataSetChanged()
    }


    private var guideItemClickListener: OnGuideItemClickListener? = null

    fun setGuideItemClickListener(listener: OnGuideItemClickListener) {
        guideItemClickListener = listener
    }

    interface OnGuideItemClickListener {
        fun guideClick(item: AppData)
        fun guideSelectAll(isSelect: Boolean)

    }


}
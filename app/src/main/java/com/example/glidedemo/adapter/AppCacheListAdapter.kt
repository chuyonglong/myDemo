package com.example.glidedemo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.glidedemo.databinding.ItemAppCacheBinding
import com.example.glidedemo.entity.AppCacheData

class AppCacheListAdapter : RecyclerView.Adapter<AppCacheListAdapter.AppCacheViewHolder>() {


    private var appCacheData: List<AppCacheData> = ArrayList()

    inner class AppCacheViewHolder(val binding: ItemAppCacheBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var appCacheDataItem: AppCacheData? = null

        init {
            binding.root.setOnClickListener {
                appCacheDataItem?.packageName?.let { packageName ->
                    onItemClickCallback?.onItemClick(
                        packageName
                    )
                }
            }
        }

        fun bindItem(item: AppCacheData) {
            appCacheDataItem = item
//            binding.appIcon.setImageBitmap(item.appIcon)
            binding.appName.text = item.appName
            binding.appSize.text = item.formattedCacheSize
            binding.isSystem.text = item.isSystemApp.toString()

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppCacheViewHolder {
        return AppCacheViewHolder(
            ItemAppCacheBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount() = appCacheData.size

    override fun onBindViewHolder(holder: AppCacheViewHolder, position: Int) {
        val item = appCacheData[position]
        holder.bindItem(item)
    }


    fun addCacheData(data: List<AppCacheData>) {
        appCacheData = data
        notifyDataSetChanged()
    }

    fun setOnItemClickCallback(callback: OnItemClickCallback) {
        this.onItemClickCallback = callback
    }

    private var onItemClickCallback: OnItemClickCallback? = null

    interface OnItemClickCallback {
        fun onItemClick(packageName: String)
    }
}
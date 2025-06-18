package com.example.glidedemo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.glidedemo.databinding.ItemLauncherListBinding

class LauncherListAdapter(val context: Context) :
    RecyclerView.Adapter<LauncherListAdapter.LauncherListViewHolder>() {

    private var launcherList = mutableListOf<ResolveInfo>()

    inner class LauncherListViewHolder(val binding: ItemLauncherListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var resolveInfoItem: ResolveInfo? = null

        init {
            binding.root.setOnClickListener {
                resolveInfoItem?.let {
                    onLauncherClickListener?.onLauncherClick(it)
                }
            }
        }

        fun bindItem(resolveInfo: ResolveInfo) {
            resolveInfoItem = resolveInfo
            binding.launcherName.text =
                resolveInfo.activityInfo.loadLabel(context.packageManager).toString()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LauncherListViewHolder {
        return LauncherListViewHolder(
            ItemLauncherListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: LauncherListViewHolder, position: Int) {
        val item = launcherList[position]
        holder.bindItem(item)
    }

    override fun getItemCount() = launcherList.size


    @SuppressLint("NotifyDataSetChanged")
    fun addLauncherList(list: MutableList<ResolveInfo>) {
        launcherList.clear()
        launcherList.addAll(list)
        notifyDataSetChanged()
    }

    private var onLauncherClickListener: OnLauncherClickListener? = null

    fun setOnLauncherClickListener(listener: OnLauncherClickListener) {
        onLauncherClickListener = listener
    }

    interface OnLauncherClickListener {
        fun onLauncherClick(resolveInfo: ResolveInfo)
    }
}
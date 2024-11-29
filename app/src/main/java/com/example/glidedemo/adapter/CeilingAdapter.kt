package com.example.glidedemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.glidedemo.bean.MediaBase
import com.example.glidedemo.bean.MediaData
import com.example.glidedemo.databinding.ItemMediaBinding
import com.example.glidedemo.extensions.loadImage

class CeilingAdapter : RecyclerView.Adapter<CeilingAdapter.CeilingViewHolder>() {

    private var ceilingList = mutableListOf<MediaData>()

    private var media: MediaData? = null


    inner class CeilingViewHolder(val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {

        }

        fun bindItem(item: MediaData) {
            media = item
            binding.myImage.loadImage(item.path)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CeilingViewHolder {
        return CeilingViewHolder(
            ItemMediaBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount() = ceilingList.size

    override fun onBindViewHolder(holder: CeilingViewHolder, position: Int) {
        val item = ceilingList[position]
        holder.bindItem(item)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMediaList(list: MutableList<MediaData>) {
        ceilingList = list
        notifyDataSetChanged()

    }
}
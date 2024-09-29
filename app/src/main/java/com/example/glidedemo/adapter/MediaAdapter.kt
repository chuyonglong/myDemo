package com.example.glidedemo.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.glidedemo.base.BaseAdapter
import com.example.glidedemo.bean.MediaBase
import com.example.glidedemo.bean.MediaData
import com.example.glidedemo.bean.MediaTitle

import com.example.glidedemo.databinding.ItemMediaBinding
import com.example.glidedemo.databinding.ItemTitleBinding

class MediaAdapter : BaseAdapter() {
    //标题
    private val ITEM_TITLE = 1001

    //图片数据
    private val ITEM_MEDIUM = 1002

    private var mediaList: MutableList<MediaBase> = mutableListOf()

    private var mediaHash = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            ITEM_TITLE -> {
                TitleViewHolder(
                    ItemTitleBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                MediaViewHolder(
                    ItemMediaBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = mediaList[position]
        when (holder.itemViewType) {
            ITEM_TITLE -> {
                (holder as TitleViewHolder).bindView(item as MediaTitle)
            }

            else -> {
                (holder as MediaViewHolder).bindView(item as MediaData)
            }
        }


    }

    override fun getItemViewType(position: Int): Int {
        return when (mediaList[position]) {
            is MediaTitle -> ITEM_TITLE
            else -> ITEM_MEDIUM
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMediaList(list: MutableList<MediaBase>) {
        if (mediaHash != list.hashCode()) {
            mediaHash = list.hashCode()
            mediaList.clear()
            mediaList = list
            notifyDataSetChanged()
        }

    }

    inner class MediaViewHolder(private val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(medium: MediaData) {
            Glide.with(binding.root.context).load(medium.path).into(binding.myImage)
        }
    }

    inner class TitleViewHolder(private val binding: ItemTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(mediaTitle: MediaTitle) {
            binding.itemRoomText.text = mediaTitle.title
        }
    }

    fun isASectionTitle(position: Int): Boolean {
        val item = mediaList.getOrNull(position) ?: return false
        return item is MediaTitle
    }
}
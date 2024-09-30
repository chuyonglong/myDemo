package com.example.glidedemo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.glidedemo.bean.MediaData
import com.example.glidedemo.bean.MediaListData
import com.example.glidedemo.bean.MediaTitle
import com.example.glidedemo.databinding.ItemMediaBinding
import com.example.glidedemo.databinding.ItemTitleBinding
import com.example.glidedemo.utils.ItemTypeEnum

class MediaListAdapter :
    ListAdapter<MediaListData, RecyclerView.ViewHolder>(ItemMediaListDiffCallback()) {
    //标题
    private val ITEM_TITLE = 1001

    //图片数据
    private val ITEM_MEDIUM = 1002
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TITLE -> {
                MediaListTitleViewHolder(
                    ItemTitleBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                MediaListDataViewHolder(
                    ItemMediaBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder.itemViewType) {
            ITEM_TITLE -> {
                (holder as MediaListTitleViewHolder).bindView(item)
            }

            else -> {
                (holder as MediaListDataViewHolder).bindView(item)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        val itemType = getItem(position).itemType
        return when (itemType) {
            ItemTypeEnum.TITLE -> ITEM_TITLE
            else -> ITEM_MEDIUM
        }
    }


    class MediaListDataViewHolder(var binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(medium: MediaListData) {
            Glide.with(binding.root.context).load(medium.path).into(binding.myImage)
        }
    }

    class MediaListTitleViewHolder(var binding: ItemTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(mediaTitle: MediaListData) {
            binding.itemRoomText.text = mediaTitle.title
        }
    }

    class ItemMediaListDiffCallback : DiffUtil.ItemCallback<MediaListData>() {
        override fun areItemsTheSame(oldItem: MediaListData, newItem: MediaListData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: MediaListData, newItem: MediaListData): Boolean {
            return oldItem == newItem
        }
    }

    fun isASectionTitle(position: Int): Boolean {
        val itemType = getItem(position).itemType
        return itemType == ItemTypeEnum.TITLE
    }
}
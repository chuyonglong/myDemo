package com.example.glidedemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.glidedemo.bean.BaseData
import com.example.glidedemo.bean.CleanUpDetailData
import com.example.glidedemo.databinding.ItemCleanupDataBinding
import com.example.glidedemo.databinding.ItemDataTitleBinding
import com.example.glidedemo.entity.CleanUpDetailTitleData
import com.example.glidedemo.extensions.byte2FitMemorySizeToString
import com.example.glidedemo.extensions.loadImage


class CleanUpDetailAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_TITLE = 0
    private val ITEM_SIMILAR = 1

    //列表数据
    private var similarPhotoList: MutableList<BaseData> = mutableListOf()
    private var itemListener: OnItemClickListener? = null

    inner class TitleViewHolder(private val binding: ItemDataTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItem(item: CleanUpDetailTitleData) {
            binding.thumbnailSection.text = item.title
        }

    }

    inner class ItemViewHolder(private val binding: ItemCleanupDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                itemListener?.onImageClick(similarPhotoList[absoluteAdapterPosition] as CleanUpDetailData)
            }
            binding.mediaSelect.setOnClickListener {
                val item = similarPhotoList[absoluteAdapterPosition] as CleanUpDetailData
                item.isSelected = !item.isSelected
                notifyItemChanged(absoluteAdapterPosition)
                itemListener?.onItemClick(item)
            }
        }

        fun bindItem(item: CleanUpDetailData) {
            binding.mediaSelect.isSelected = item.isSelected
            binding.mediaGrid.loadImage(item.path)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TITLE -> TitleViewHolder(
                ItemDataTitleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> ItemViewHolder(
                ItemCleanupDataBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

        }
    }

    override fun getItemCount(): Int {
        return similarPhotoList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = similarPhotoList[position]
        when (holder.itemViewType) {
            ITEM_TITLE -> (holder as TitleViewHolder).bindItem(item as CleanUpDetailTitleData)
            else -> (holder as ItemViewHolder).bindItem(item as CleanUpDetailData)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun overallRefresh(list: MutableList<BaseData>) {
        similarPhotoList.clear()
        similarPhotoList.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun localRefresh(list: MutableList<BaseData>, position: Int = -1) {
        similarPhotoList.clear()
        similarPhotoList.addAll(list)
        if (position != -1) {
            notifyItemInserted(position)
        } else {
            if (similarPhotoList.size < 4) {
                notifyItemRangeChanged(0, 3)
            } else {
                notifyItemRangeChanged(similarPhotoList.size - 4, similarPhotoList.size - 1)
            }
        }

    }


    override fun getItemViewType(position: Int): Int {
        return when (similarPhotoList[position]) {
            is CleanUpDetailTitleData -> ITEM_TITLE
            else -> ITEM_SIMILAR
        }
    }

    fun isSimilarTitle(position: Int) =
        similarPhotoList.getOrNull(position) is CleanUpDetailTitleData

    /**
     * 获取所有图片的list
     */
    fun getSelectedItem(): ArrayList<CleanUpDetailData> {
        val list = arrayListOf<CleanUpDetailData>()
        for (item in similarPhotoList) {
            if (item is CleanUpDetailData && item.isSelected) {
                list.add(item)
            }
        }
        return list
    }

    /**
     * 获取选中的图片的数量和内存占用大小
     */
    fun getSelectedSize(callback: (Int, String) -> Unit) {
        var count = 0
        var countSize = 0L
        for (item in similarPhotoList) {
            if (item is CleanUpDetailData && item.isSelected) {
                countSize += item.size
                count++
            }
        }
        callback(count, countSize.byte2FitMemorySizeToString())
    }


    @SuppressLint("NotifyDataSetChanged")
    fun similarClear() {
        similarPhotoList.clear()
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.itemListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(item: CleanUpDetailData)

        fun onImageClick(item: CleanUpDetailData)
    }

}

package com.example.glidedemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.glidedemo.base.BaseAdapter
import com.example.glidedemo.databinding.ItemTitleBinding
import com.example.glidedemo.entity.TextData

class RoomAdapter : BaseAdapter() {
    private var textList = mutableListOf<TextData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TextViewHolder(
            ItemTitleBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = textList.getOrNull(position)
        item?.let {
            (holder as TextViewHolder).bindItem(it)
        }

    }

    override fun getItemCount() = textList.size


    inner class TextViewHolder(val binding: ItemTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {

        }

        fun bindItem(textData: TextData) {
            binding.itemRoomText.text = textData.textStr
            binding.itemRoomText.startColor = textData.textStartColor
            binding.itemRoomText.endColor = textData.textEndColor
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setRoomList(strList: MutableList<TextData>) {
        textList = strList
        notifyDataSetChanged()
    }
}
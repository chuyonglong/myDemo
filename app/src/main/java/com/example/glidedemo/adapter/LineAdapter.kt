package com.example.glidedemo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.glidedemo.adapter.LineAdapter.LineViewHolder
import com.example.glidedemo.databinding.ItemTitleBinding
import com.example.glidedemo.entity.TextData


class LineAdapter : ListAdapter<TextData, LineViewHolder>(ItemDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        return LineViewHolder(ItemTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        val model = getItem(position)
        holder.binding.itemRoomText.text = model.textStr
        holder.binding.itemRoomText.startColor =model.textStartColor
        holder.binding.itemRoomText.endColor = model.textEndColor
    }

//    override fun onBindViewHolder(holder: LineViewHolder, position: Int, payloads: MutableList<Any>) {
//        if (payloads.contains("ping")) {
//            val model = getItem(position)
////            holder.binding.tvDelayTime.text = model.delay
//        } else {
//            super.onBindViewHolder(holder, position, payloads)
//        }
//    }

    class ItemDiffCallback : DiffUtil.ItemCallback<TextData>() {
        override fun areItemsTheSame(oldItem: TextData, newItem: TextData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TextData, newItem: TextData): Boolean {
            return oldItem == newItem
        }
    }

    class LineViewHolder(var binding: ItemTitleBinding) : RecyclerView.ViewHolder(binding.root)
}

package com.example.glidedemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.glidedemo.bean.CleanUpScanData
import com.example.glidedemo.bean.ScanMediaData
import com.example.glidedemo.databinding.ItemCleanupScanBinding
import com.example.glidedemo.databinding.ItemScanShowBinding
import com.example.glidedemo.extensions.loadImage
import com.example.glidedemo.views.GalleryLinearLayoutManager

class CleanUpMainAdapter : RecyclerView.Adapter<CleanUpMainAdapter.CleanUpViewHolder>() {
    private var similarScanList: ArrayList<CleanUpScanData> = arrayListOf()

    private var similarScanListHash = 0

    private var itemListener: OnItemClickListener? = null

    inner class CleanUpViewHolder(private val binding: ItemCleanupScanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CleanUpScanData) {
            binding.tvScanTitle.text = item.title
            binding.tvScanSize.text = item.sizeStr
            binding.tvScanContent.text = item.content
            val innerAdapter = InnerAdapter(item.pathList)
            binding.rvScanList.apply {
                layoutManager = GalleryLinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                adapter = innerAdapter
            }
            binding.root.setOnClickListener {
                itemListener?.onItemClick(item)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CleanUpViewHolder {
        val viewBinding =
            ItemCleanupScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CleanUpViewHolder(viewBinding)
    }



    override fun onBindViewHolder(holder: CleanUpViewHolder, position: Int) {
        val item = similarScanList[position]
        holder.bind(item)
    }

    override fun getItemCount() = similarScanList.size


    @SuppressLint("NotifyDataSetChanged")
    fun setSimilarScanList(list: ArrayList<CleanUpScanData>) {
        val currentHashCode = list.clone().hashCode()
        if (currentHashCode != similarScanListHash) {
            similarScanListHash = currentHashCode
            similarScanList.clear()
            similarScanList.addAll(list)
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        similarScanListHash = 0
        similarScanList.clear()
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.itemListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(item: CleanUpScanData)
    }


    class InnerAdapter(private val innerList: List<ScanMediaData>) :
        RecyclerView.Adapter<InnerAdapter.InnerViewHolder>()  {

        inner class InnerViewHolder(private val binding: ItemScanShowBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(item: ScanMediaData) {
                binding.ivCleanupPhoto.loadImage(item.path)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
            val viewBinding =
                ItemScanShowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return InnerViewHolder(viewBinding)
        }



        override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
            val item = innerList[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int {
            return innerList.size
        }
    }
}

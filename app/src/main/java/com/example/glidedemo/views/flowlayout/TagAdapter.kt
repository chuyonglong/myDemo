package com.example.glidedemo.views.flowlayout

import android.util.Log
import android.view.View
import java.util.Arrays

abstract class TagAdapter<T> {
    private var mTagDatas: List<T>?
    private var mOnDataChangedListener: OnDataChangedListener? = null
    val preCheckedList: HashSet<Int> = HashSet()

    constructor(datas: List<T>?) {
        mTagDatas = datas
    }

    constructor(datas: Array<T>) {
        mTagDatas = ArrayList(Arrays.asList(*datas))
    }

    interface OnDataChangedListener {
        fun onChanged()
    }

    fun setOnDataChangedListener(listener: OnDataChangedListener?) {
        mOnDataChangedListener = listener
    }

    fun setSelectedList(vararg poses: Int) {
        val set: MutableSet<Int> = HashSet()
        for (pos in poses) {
            set.add(pos)
        }
        setSelectedList(set)
    }

    fun setSelectedList(set: Set<Int>?) {
        preCheckedList.clear()
        if (set != null) {
            preCheckedList.addAll(set)
        }
        notifyDataChanged()
    }


    val count: Int
        get() = if (mTagDatas == null) 0 else mTagDatas!!.size

    fun notifyDataChanged() {
        if (mOnDataChangedListener != null) {
            mOnDataChangedListener!!.onChanged()
        }
    }

    fun getItem(position: Int): T {
        return mTagDatas!![position]
    }

    abstract fun getView(parent: FlowLayout?, position: Int, t: Any): View?


    fun onSelected(position: Int, view: View?) {
        Log.d("zhy", "onSelected $position")
    }

    fun unSelected(position: Int, view: View?) {
        Log.d("zhy", "unSelected $position")
    }

    fun setSelected(position: Int, t: Any?): Boolean {
        return false
    }
}

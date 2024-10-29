package com.example.glidedemo.views.pattern_locker

import android.graphics.Canvas
import com.example.glidedemo.views.pattern_locker.CellBean

/**
 * Created by hsg on 22/02/2018.
 */

interface IIndicatorLinkedLineView {
    /**
     * 绘制指示器连接线
     *
     * @param canvas
     * @param hitIndexList
     * @param cellBeanList
     * @param isError
     */
    fun draw(canvas: Canvas,
             hitIndexList: List<Int>,
             cellBeanList: List<CellBean>,
             isError: Boolean)
}
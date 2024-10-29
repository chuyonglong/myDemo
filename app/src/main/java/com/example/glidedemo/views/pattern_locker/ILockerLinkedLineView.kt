package com.example.glidedemo.views.pattern_locker

import android.graphics.Canvas
import com.example.glidedemo.views.pattern_locker.CellBean

/**
 * Created by hsg on 22/02/2018.
 */

interface ILockerLinkedLineView {
    /**
     * 绘制图案密码连接线
     *
     * @param canvas
     * @param hitIndexList
     * @param cellBeanList
     * @param endX
     * @param endY
     * @param isError
     */
    fun draw(canvas: Canvas,
             hitIndexList: List<Int>,
             cellBeanList: List<CellBean>,
             endX: Float,
             endY: Float,
             isError: Boolean)
}
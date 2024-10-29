package com.example.glidedemo.views.pattern_locker

import android.graphics.Canvas
import com.example.glidedemo.views.pattern_locker.CellBean

/**
 * Created by hsg on 22/02/2018.
 */

interface IHitCellView {
    /**
     * 绘制已设置的每个图案的样式
     *
     * @param canvas
     * @param cellBean
     * @param isError
     */
    fun draw(canvas: Canvas, cellBean: CellBean, isError: Boolean)
}

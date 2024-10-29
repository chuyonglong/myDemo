package com.example.glidedemo.views.pattern_locker

import android.graphics.Canvas
import android.graphics.Paint
import com.example.glidedemo.views.pattern_locker.CellBean

/**
 * Created by hsg on 22/02/2018.
 */

open class DefaultLockerNormalCellView(val styleDecorator: DefaultStyleDecorator) :
    INormalCellView {
    private val paint: Paint by lazy {
        DefaultConfig.createPaint()
    }

    init {
        this.paint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas, cellBean: CellBean) {
        val saveCount = canvas.save()

        // draw outer circle
        this.paint.color = this.styleDecorator.normalColor
        canvas.drawCircle(cellBean.centerX, cellBean.centerY, cellBean.radius, this.paint)


        // draw fill circle
        this.paint.color = this.styleDecorator.fillColor
        val radius = (cellBean.radius - this.styleDecorator.lineWidth) * 0.6F
        canvas.drawCircle(cellBean.centerX, cellBean.centerY, radius, this.paint)

        canvas.restoreToCount(saveCount)
    }
}

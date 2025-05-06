package com.example.freshtrack.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.freshtrack.ui.components.OverlayView

class OverlayView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#AA000000")
    }

    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)


        val rectWidth = width * 0.8f
        val rectHeight = height * 0.25f
        val left = (width - rectWidth) / 2
        val top = (height - rectHeight) / 2
        val right = left + rectWidth
        val bottom = top + rectHeight


        canvas.drawRect(left, top, right, bottom, clearPaint)


        val centerY = (top + bottom) / 2

        val redLinePaint = Paint().apply {
            color = Color.RED
            strokeWidth = 4f
            isAntiAlias = true
        }

        canvas.drawLine(left, centerY, right, centerY, redLinePaint)
    }


    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }
}

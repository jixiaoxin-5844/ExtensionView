package com.extension.lib_view.doodle.action

import android.graphics.Canvas

abstract class BaseAction constructor(
    protected val startX: Float,
    protected val startY: Float,
    protected var color: Int = 0,
    protected var size: Int = 0,
    protected var stopX: Float = startX,
    protected var stopY: Float = startY
) {
    abstract fun onStart(canvas: Canvas)
    abstract fun onMove(mx: Float,my: Float)
    abstract fun onDraw(canvas: Canvas)
}
package com.extension.lib_view.doodle.action

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path


/**
 * 路径绘制
 * 用于画笔
 * */
class PathAction(
    startX: Float, startY: Float, color: Int, size: Int
) : BaseAction(startX, startY, color, size) {

    constructor():this(0f,0f,0,0)

    private val path = Path().apply {
        moveTo(startX,startY)
    }
    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        this.color = color
        strokeWidth = size.toFloat()
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    override fun onStart(canvas: Canvas) {
        onDraw(canvas)
    }

    override fun onMove(mx: Float, my: Float) {
        //TODO 验证下 这个方法绘制完会改变当前点的位置吗    按照这个写法是会的
        path.lineTo(mx, my)  //绘制当前点到指定点的线
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
        canvas.drawPoint(startX, startY, paint)
    }
}
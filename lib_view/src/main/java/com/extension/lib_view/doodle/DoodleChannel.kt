package com.extension.lib_view.doodle

import android.graphics.Color
import com.extension.lib_view.doodle.action.BaseAction
import java.util.concurrent.CopyOnWriteArrayList


/*
* 画板控制
* */
class DoodleChannel {
    var mType = 0 // 当前的形状类型
    var action: BaseAction? = null    // 当前的形状对象
    var mPaintColor = Color.BLACK    //当前画笔颜色
    var mPaintSize = 8   //当前画笔大小
    var lastPaintColor = mPaintColor // 上一次使用的画笔颜色（橡皮擦切换回形状时，恢复上次的颜色）
    var lastPaintSize = mPaintSize   // 上一次使用的画笔粗细（橡皮擦切换回形状时，恢复上次的粗细）

    /**
     * 记录所有形状的列表
     */
    val actions = CopyOnWriteArrayList<BaseAction>()

    /**
     * 设置当前画笔的形状
     */
    fun setType(type: Int) {
        /*if (this.mType == SupportActionType.getInstance().getEraserType()) {
            // 从橡皮擦切换到某种形状，恢复画笔颜色，画笔粗细
            mPaintColor = lastPaintColor
            mPaintSize = lastPaintSize
        }*/
        this.mType = type
    }

    /**
     * 设置当前画笔为橡皮擦
     */
    /*   fun setEraseType(bgColor: Int, size: Int) {
           mType = SupportActionType.getInstance().getEraserType()
           lastPaintColor = mPaintColor // 备份当前的画笔颜色
           lastPaintSize = mPaintSize // 备份当前的画笔粗细
           mPaintColor = bgColor
           if (size > 0) {
               mPaintSize = size
           }
       }*/

    /**
     * 设置当前画笔的颜色
     */
    fun setPaintColor(color: Int) {
        // 如果正在使用橡皮擦，那么不能更改画笔颜色
        /*   if (mType == SupportActionType.getInstance().getEraserType()) {
               return
           }*/
        mPaintColor = color
    }

    /**
     * 设置画笔的粗细
     */
    fun setPaintSize(size: Int) {
        if (size > 0) {
            mPaintSize = size
        } else {
            throw IllegalStateException("Paint Size not can <= 0")
        }
    }
}
package com.extension.lib_view.doodle

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.extension.lib_view.doodle.action.BaseAction
import com.extension.lib_view.doodle.action.PathAction
import kotlin.math.abs
import kotlin.math.roundToInt

class DoodleView : SurfaceView, SurfaceHolder.Callback, LifecycleObserver {
    private val TAG = "MyDoodleViewZZZ"
    private val surfaceHolder: SurfaceHolder = holder

    private var enableView = true     //自己是否可以手动绘制
    private var isSurfaceViewCreated = false

    private val bgColor = Color.WHITE // 背景颜色
    private val paintColor = Color.BLACK // 默认画笔颜色 数据同步他人画笔时需要保存
    private val paintChannel: DoodleChannel = DoodleChannel()  // 个人绘图通道
    private val doodleControl = DoodleControl() //画笔控制

    private val downPoint = PointF()

    private var lastX = 0.0f
    private var lastY = 0.0f
    private var bitmap: Bitmap? = null

    private var xZoom = 1.0f // 收发数据时缩放倍数（归一化）
    private var yZoom = 1.0f


    init {
        surfaceHolder.addCallback(this)
    }

    private lateinit var mLifecycleOwner: LifecycleOwner
    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        mLifecycleOwner = lifecycleOwner
        mLifecycleOwner.lifecycle.addObserver(this)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        // val mTypeArray = context.obtainStyledAttributes(attrs, R.styleable.BadgeView)

        /*  bgColor = mTypeArray.getColor(R.styleable.BadgeView_bgColor, Color.RED)
          textColor = mTypeArray.getColor(R.styleable.BadgeView_textColor, Color.WHITE)
          textSize = mTypeArray.getDimension(R.styleable.BadgeView_textSize, SizeUtils.dp2px(14f).toFloat())
          badgeNum = mTypeArray.getInteger(R.styleable.BadgeView_badgeNum, 0)
  */
        //  mTypeArray.recycle()

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated ->")
        isSurfaceViewCreated = true
        //恢复历史绘制数据
        addDraw {
            drawHistoryActions(it)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.i(TAG, "surfaceChanged , width = $width, height = $height")
        xZoom = width.toFloat()
        yZoom = height.toFloat()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed ->")
        isSurfaceViewCreated = false

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!enableView) {
            return true
        }
        event?.action?.run {
            if (this == MotionEvent.ACTION_CANCEL) {
                return false
            }
            val touchX = event.x
            val touchY = event.y

            Log.d(TAG, "onTouchEvent -> x=$touchX, y=$touchY")

            when (this) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(TAG, "onTouchEvent -> ACTION_DOWN")
                    onPaintActionStart(touchX, touchY)
                    downPoint.x = event.x
                    downPoint.y = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    Log.d(
                        TAG,
                        "onTouchEvent -> ACTION_MOVE : scaleX:$scaleX event.x:${event.x} downPoint.x:${downPoint.x}"
                    )
                    onPaintActionMove(touchX, touchY)
                    //TODO 这个if什么意思 滑动？
                    if (scaleX > 1 && abs(event.x - downPoint.x) > 5) {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    Log.d(TAG, "onTouchEvent -> ACTION_UP")
                    onPaintActionEnd(touchX, touchY)
                }
            }

        }
        return true
    }


    // *************************** 基础绘图封装  ********************************

    //Start
    private fun onPaintActionStart(x: Float, y: Float) {
        onActionStart(x, y)
    }

    private fun onActionStart(x: Float, y: Float) {
        addDraw {
            lastX = x
            lastY = y
            drawHistoryActions(it)
            //paintChannel.action = PathAction(x, y, paintChannel.mPaintColor, paintChannel.mPaintSize)
            paintChannel.action = getAction(x, y)
            //点的快的话 只有 ACTION_DOWN  -> ACTION_UP  没有ACTION_MOVE  所以这里draw一下
            paintChannel.action?.onDraw(it)
        }
    }

    //Move
    private fun onPaintActionMove(x: Float, y: Float) {
        onActionMove(x, y)
    }

    private fun onActionMove(x: Float, y: Float) {
        addDraw {
            drawHistoryActions(it)
            if (paintChannel.action == null) {
                // 有可能action被清空，此时收到move，重新补个start
                onPaintActionStart(x, y)
            }
            paintChannel.action?.onMove(x, y)
            paintChannel.action?.onDraw(it)
        }
    }

    //End
    private fun onPaintActionEnd(x: Float, y: Float) {
        onActionEnd()
    }

    private fun onActionEnd() {
        if (paintChannel.action == null) {
            return
        }
        paintChannel.actions.add(paintChannel.action)
        paintChannel.action = null
    }

    /*
    * 绘制历史数据
    * */
    private fun drawHistoryActions(canvas: Canvas) {
        //1 绘制背景
        if (bitmap != null) {
            drawBitmapBg(canvas)
        } else {
            canvas.drawColor(bgColor)
        }
        //2 绘制存储内容
        paintChannel.actions.forEach { it2 ->
            it2.onDraw(canvas)
        }
        // 绘制当前
        paintChannel.action?.onDraw(canvas)
    }


    // 涂鸦板背景颜色
    /*   private fun drawBackground() {
           addDraw {
               it.drawColor(bgColor)
           }
       }*/

    /**
     * 创建Action
     * */
    private fun getAction(startX: Float, startY: Float): BaseAction {
        return when (doodleControl.action) {
            is PathAction -> {
                PathAction(startX, startY, paintChannel.mPaintColor, paintChannel.mPaintSize)
            }
            else -> {
                PathAction(startX, startY, paintChannel.mPaintColor, paintChannel.mPaintSize)
            }
        }
    }

    /*
    * 添加绘制任务
    * */
    private fun addDraw(drawJob: ((canvas: Canvas) -> Unit)) {
        surfaceHolder.lockCanvas()?.let {
            drawJob.invoke(it)
            surfaceHolder.unlockCanvasAndPost(it)
        }
    }

    // 绘制bitmap背景
    private fun drawBitmapBg(canvas: Canvas) {
        val bitmapWidth = bitmap!!.width
        val bitmapHeight = bitmap!!.height
        val canvasRatio = (canvas.height.toFloat() / canvas.width * 100).roundToInt()
            .toFloat() / 100
        val bitmapRatio = (bitmapHeight.toFloat() / bitmapWidth * 100).roundToInt().toFloat() / 100
        val matrix = Matrix()
        //获取缩放比例
        if (bitmapRatio > canvasRatio) {
            matrix.postScale(canvasRatio, bitmapRatio)
        } else {
            matrix.postScale(
                1.0f * canvas.getWidth() / bitmapWidth,
                1.0f * canvas.getHeight() / bitmapHeight
            )
        }
        //按缩放比例生成适应屏幕的新的bitmap；
        val dstbmp = Bitmap.createBitmap(
            bitmap!!, 0, 0, bitmapWidth,
            bitmapHeight, matrix, true
        )
        val src = Rect(0, 0, dstbmp.width, dstbmp.height)
        // Rect用于居中显示
        if (canvasRatio > bitmapRatio) {
            val dest = Rect(
                0, canvas.height / 2 - dstbmp.height / 2,
                canvas.width, canvas.height / 2 + dstbmp.height / 2
            )
            canvas.drawBitmap(dstbmp, src, dest, null)
        } else {
            val dest = Rect(
                canvas.width / 2 - dstbmp.width / 2, 0,
                canvas.width / 2 + dstbmp.width / 2, canvas.height
            )
            canvas.drawBitmap(dstbmp, src, dest, null)
        }
    }

    // ******************* 生命周期方法  *******************
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        /*  addDraw {
              drawHistoryActions(it)
          }*/
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        mLifecycleOwner.lifecycle.removeObserver(this)
    }

    // ******************* 暴露给外部的方法  *******************

    fun setEnableView(enableView: Boolean) {
        this.enableView = enableView
    }

    /*
    *   设置bitmap背景
    * */
    fun setBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
    }

    /**
     * 设置当前选中画笔类型
     * */
    fun setDoodleAction(action: BaseAction) {
        doodleControl.action = action
    }

    /*
    * 保存内容成图片
    * */
    /*  fun saveToPhoto(){
          addDraw {
              post {
                  val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                  val canvas = Canvas(bitmap)
                  draw(canvas)
                  val file = File(PathControl.getRandomFilePath())

                  val save = ImageUtils.save(bitmap, file, Bitmap.CompressFormat.JPEG, true)

              }
          }
      }*/

}
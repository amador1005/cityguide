package com.yuchen.cityguide.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.annotation.Px
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import com.yuchen.cityguide.R

class SlidingMenu @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                            defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private val mScroller: Scroller
    private var mDuration: Int = 0
    lateinit private var mTextPaint: Paint
    lateinit private var mFontMetrics: Paint.FontMetrics
    private var mSlidingMenuHeight: Int = 0
    private var mTextSize: Int = 0
    private var mTextColor: Int = 0
    private var mTextSelectedColor: Int = 0

    private var mTextMargin: Int = 0
    lateinit private var mBackground: Drawable
    lateinit private var mCursor: Drawable
    private var mPaddingLeft: Int = 0
    private var mPaddingTop: Int = 0
    private var mPaddingRight: Int = 0
    private var mPaddingBottom: Int = 0
    private var mCursorPosition: Int = 0
    private var data: Array<String>? = null
    private var mTextHeight: Int = 0
    private var mDownX: Float = 0.toFloat()
    private val mTouchSlop: Int
    private var mCursorX: Int = 0
    private var isCursorSlide: Boolean = false

    private var mListener: OnMenuSelectedListener? = null

    init {
        parseConfig(context, attrs)
        initPaint()
        mScroller = Scroller(context, DecelerateInterpolator())
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    private fun parseConfig(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu)
        mSlidingMenuHeight = a.getDimension(R.styleable.SlidingMenu_sm_height, dp2px(context,
                DEFAULT_SM_HEIGHT_DP).toFloat()).toInt()
        mBackground = a.getDrawable(R.styleable.SlidingMenu_sm_background) ?: ContextCompat.getDrawable(context, R.drawable.tray)
        mCursor = a.getDrawable(R.styleable.SlidingMenu_sm_cursor) ?: ContextCompat.getDrawable(context, R.drawable.slider)
        mTextColor = a.getColor(R.styleable.SlidingMenu_sm_textColor, DEFAULT_TEXT_COLOR)
        mTextSelectedColor = a.getColor(R.styleable.SlidingMenu_sm_textSelectedColor,
                DEFAULT_TEXT_SELECTED_COLOR)
        mTextSize = a.getDimension(R.styleable.SlidingMenu_sm_textSize, dp2px(context, DEFAULT_TEXT_SIZE_DP).toFloat()).toInt()
        mTextMargin = a.getDimension(R.styleable.SlidingMenu_sm_textMargin, dp2px(context, DEFAULT_TEXT_MARGIN_DP).toFloat()).toInt()
        mDuration = a.getInteger(R.styleable.SlidingMenu_sm_duration, DEFAULT_DURATION)
        mPaddingLeft = paddingLeft
        mPaddingTop = paddingTop
        mPaddingRight = paddingRight
        mPaddingBottom = paddingBottom
        mCursorX = mPaddingLeft
        a.recycle()
    }

    private fun initPaint() {
        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint.textSize = mTextSize.toFloat()
        mTextPaint.color = mTextColor
        mTextPaint.textAlign = Paint.Align.CENTER
        mTextPaint.strokeWidth = 1.0f
        mFontMetrics = mTextPaint.fontMetrics
        mTextHeight = (mFontMetrics.descent - mFontMetrics.ascent).toInt()
    }

    fun setData(data: Array<String>) {
        this.data = data
        invalidate()
    }

    fun setSelect(position: Int) {
        if (data == null) {
            throw NullPointerException("the data of SlidingMenu is null")
        }
        if (position < 0 || position >= data!!.size) {
            throw ArrayIndexOutOfBoundsException(position.toString() + " is out of range " + data!!.size)
        }
        post {
            mCursorPosition = position
            mCursorX = calcX(position)
            invalidate()
        }
    }

    fun setOnMenuSelectedListener(listener: OnMenuSelectedListener) {
        this.mListener = listener
    }

    override fun setPadding(@Px left: Int, @Px top: Int, @Px right: Int, @Px bottom: Int) {
        super.setPadding(left, top, right, bottom)
        mPaddingLeft = left
        mPaddingTop = top
        mPaddingRight = right
        mPaddingBottom = bottom
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightNeed = mSlidingMenuHeight + mPaddingTop + mPaddingBottom + mTextMargin
        if (heightMode == View.MeasureSpec.EXACTLY) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(heightSize, View.MeasureSpec.EXACTLY)
        } else {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(heightNeed, View.MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawCursor(canvas)
        drawText(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        val left = mPaddingLeft
        val top = mPaddingTop
        val right = measuredWidth - mPaddingRight
        val bottom = top + mSlidingMenuHeight
        val bgRect = Rect(left, top, right, bottom)
        mBackground.bounds = bgRect
        mBackground.draw(canvas)
    }

    private fun drawCursor(canvas: Canvas) {
        val step = calStep()
        val centerX = mCursorX
        val left = (centerX - step / 2).toInt()
        val right = (left + step).toInt()
        val top = mPaddingTop
        val bottom = top + mSlidingMenuHeight
        //Log.d(TAG, "drawCursor top $top, bottom $bottom, left $left, right $right")
        val rect = Rect(left, top, right, bottom)
        mCursor.bounds = rect
        mCursor.draw(canvas)
    }

    private fun checkCursor() {
        val step = calStep()
        val minX = (mPaddingLeft + step / 2).toInt()
        val maxX = (measuredWidth - mPaddingRight - step / 2).toInt()
        if (mCursorX < minX) {
            mCursorX = minX
            return
        }
        if (mCursorX > maxX) {
            mCursorX = maxX
        }
    }

    private fun drawText(canvas: Canvas) {
        data?.let {
            if (it.isNotEmpty()) {
                val baseline = (mPaddingTop + mSlidingMenuHeight / 2 + mTextHeight / 2 - mFontMetrics
                        .descent)
                for (i in it.indices) {
                    if (i == mCursorPosition) {
                        mTextPaint.color = mTextSelectedColor
                    } else {
                        mTextPaint.color = mTextColor
                    }
                    val x = calcX(i)
                    canvas.drawText(data!![i], x.toFloat(), baseline, mTextPaint)
                }
            }
        }
    }

    private fun calStep(): Float {
        var step = measuredWidth * 1.0f - mPaddingLeft.toFloat() - mPaddingRight.toFloat()
        if (data != null && data!!.size > 1) {
            step = (measuredWidth * 1.0f - mPaddingLeft.toFloat() - mPaddingRight.toFloat()) / (data!!.size)
        }
        return step
    }

    private fun calcX(position: Int): Int {
        val step = calStep()
        val minX = (mPaddingLeft + step / 2).toInt()
        if (data == null || data!!.size < 1) {
            return minX
        }
        return (minX + step * position).toInt()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        mDownX = event.x
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                }
                mCursorX = mDownX.toInt()
                checkCursor()
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true)
                mCursorX = mDownX.toInt()
                checkCursor()
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                mCursorPosition = calcCursorPosition()
                val slideX = calcX(mCursorPosition)
                val delta = (slideX - mDownX).toInt()
                if (shouldSlide(delta)) {
                    isCursorSlide = true
                    mScroller.startScroll(mDownX.toInt(), 0, delta, 0, mDuration)
                    invalidate()
                } else {
                    mCursorX = slideX
                    invalidate()
                    mListener?.onSelect(mCursorPosition)
                }
            }
        }
        return true
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mCursorX = mScroller.currX
            invalidate()
        } else {
            if (isCursorSlide) {
                mListener?.onSelect(mCursorPosition)
                isCursorSlide = false
            }
        }
    }

    private fun calcCursorPosition(): Int {
        if (data == null || data!!.size == 0) {
            return 0
        }
        var step = calStep()
        var pos = ((mDownX - mPaddingLeft) / step).toInt()
        return if (pos < 0)
            0
        else if (pos > data!!.size - 1)
            data!!.size - 1
        else
            pos
    }

    private fun shouldSlide(distance: Int): Boolean {
        return Math.abs(distance) > mTouchSlop
    }

    private fun dp2px(context: Context, dpValue: Int): Int {
        return (context.resources.displayMetrics.density * dpValue + 0.5f).toInt()
    }

    companion object {
        private val DEFAULT_SM_HEIGHT_DP = 48
        private val DEFAULT_TEXT_SIZE_DP = 14
        private val DEFAULT_TEXT_MARGIN_DP = 10
        private val DEFAULT_DURATION = 300
        private val DEFAULT_TEXT_COLOR = Color.parseColor("#999999")
        private val DEFAULT_TEXT_SELECTED_COLOR = Color.parseColor("#FCB54C")
        private val TAG = javaClass.simpleName

    }

    interface OnMenuSelectedListener {
        fun onSelect(position: Int)
    }


}

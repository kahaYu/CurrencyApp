package com.raywenderlich.currencyapp.ui.customRefreshLayout

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import androidx.core.content.ContextCompat
import com.raywenderlich.currencyapp.R
import java.lang.NullPointerException


class CircleProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseProgressBar(context, attrs, defStyleAttr){

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var backColor = ContextCompat.getColor(context, R.color.transparent)
    private var frontColor = ContextCompat.getColor(context, R.color.teal_700)
    private val backPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var borderWidth = dp2px(4).toFloat()

    private var size = dp2px(80)
    private var mIndeterminateSweep: Float = 0f
    private var mStartAngle: Float = 0f

    private var mRect: RectF? = null
    private var progressAnimator: ValueAnimator? = null

    init {
        paint.color = frontColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth
        backPaint.color = backColor
        backPaint.style = Paint.Style.STROKE
        backPaint.strokeWidth = borderWidth

        mRect = RectF()
        mIndeterminateSweep = 170f

    }

    fun setBorderWidth(width: Int){
        paint.strokeWidth = dp2px(width).toFloat()
        backPaint.strokeWidth = dp2px(width).toFloat()
    }

    fun setColors(backColor: Int, frontColor: Int){
        paint.color = frontColor
        backPaint.color = backColor
    }

    fun setSize(px: Int){
        size = (px * context.resources.displayMetrics.density).toInt()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
            mRect?.apply {
                left = ((width - size/2)/2).toFloat()
                    top = mParent?.DRAG_MAX_DISTANCE?.div( 1.5f) ?: 40f
                right = mRect?.left!! + size/2
                bottom = mRect?.top!! + size/2
            }

        canvas?.drawArc(mRect!!, 270f, 360f, false, backPaint)

        if(isLoading)
            canvas?.drawArc(mRect!!, mStartAngle, mIndeterminateSweep, false, paint)
        else
            drawProgress(canvas!!)
    }


    private fun drawProgress(canvas: Canvas){
        canvas.drawArc(mRect!!, 270f, mPercent*3.6f, false, paint)
    }

    override fun setParent(parent: IGRefreshLayout) {
        mParent = parent
    }

    override fun setPercent(percent: Float) {
        mPercent = if (percent >= 100f) 100f else percent
        invalidate()
    }

    override fun start() {
        isLoading = true
        resetAnimation()
    }

    override fun stop() {
        stopAnimation()
    }

    private fun resetAnimation(){
        if(progressAnimator != null && progressAnimator!!.isRunning)
            progressAnimator?.cancel()

        progressAnimator = ValueAnimator.ofFloat(0f, 360f)
        progressAnimator?.duration = 500
        progressAnimator?.interpolator = LinearInterpolator()
        progressAnimator?.addUpdateListener {
            mStartAngle = it.animatedValue as Float
            invalidate()
        }
        progressAnimator?.start()
        progressAnimator?.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                resetAnimation()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationStart(p0: Animator?) {

            }

        })
    }

    private fun stopAnimation(){
        isLoading = false

        if(progressAnimator != null) {
            progressAnimator?.cancel()
            progressAnimator?.removeAllListeners()
            progressAnimator = null
        }
    }
}
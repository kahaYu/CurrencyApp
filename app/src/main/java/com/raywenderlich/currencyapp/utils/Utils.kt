package com.raywenderlich.currencyapp.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.animation.*
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.model.Rate
import uz.jamshid.library.IGRefreshLayout
import uz.jamshid.library.progress_bar.BaseProgressBar
import uz.jamshid.library.progress_bar.CircleProgressBar
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class CustomCircleProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseProgressBar(context, attrs, defStyleAttr){

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var backColor = resources.getColor(R.color.transparent)
    private var frontColor = resources.getColor(R.color.teal_700)
    private val backPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var borderWidth = dp2px(4).toFloat()

    private var size = dp2px(80)
    private var mIndeterminateSweep: Float = 0f
    private var mStartAngle: Float = 0f

    private var mRect: RectF? = null
    private var progressAnimator: ValueAnimator? = null

    init {
        setParent(IGRefreshLayout(context))
        paint.color = frontColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth
        backPaint.color = backColor
        backPaint.style = Paint.Style.STROKE
        backPaint.strokeWidth = borderWidth

        mRect = RectF()
        mIndeterminateSweep = 185f
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
            top = mParent.DRAG_MAX_DISTANCE / 3f
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

class AutoClearedValue<T : Any>(val fragment: Fragment) : ReadWriteProperty<Fragment, T> {
    private var _value: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            _value = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return _value ?: throw IllegalStateException(
            "should never call auto-cleared-value get when it might not be available"
        )
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        _value = value
    }
}


fun Activity.hideSystemUI() {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.let {
            // Default behavior is that if navigation bar is hidden, the system will "steal" touches
            // and show it again upon user's touch. We just want the user to be able to show the
            // navigation bar by swipe, touches are handled by custom code -> change system bar behavior.
            // Alternative to deprecated SYSTEM_UI_FLAG_IMMERSIVE.
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // Finally, hide the system bars, alternative to View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            // and SYSTEM_UI_FLAG_FULLSCREEN.
            it.hide(WindowInsets.Type.navigationBars())
            //window.statusBarColor = getColor(R.color.transparent)
            //window.setDecorFitsSystemWindows(false)
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                // Do not let system steal touches for showing the navigation bar
                View.SYSTEM_UI_FLAG_IMMERSIVE // Убирает только низ. Верх постоянно включен.
                        // Hide the nav bar and status bar
                        //or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        //View.SYSTEM_UI_FLAG_FULLSCREEN // Убирает и низ и верх. Верх по свайпу появляется.
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                // Keep the app content behind the bars even if user swipes them up
                //or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                //or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                //or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
        // make navbar translucent - do this already in hideSystemUI() so that the bar
        // is translucent if user swipes it up
    }
}

const val BASE_URL = "https://developerhub.alfabank.by:8273/partner/1.0.1/"

object Codes {
    const val USD = 840
    const val EUR = 978
    const val RUB = 643
}

fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}

fun getDateTime(day: Day): Date {

    val calendar = Calendar.getInstance()

    return when (day) {
        Day.TODAY -> calendar.time
        Day.TOMORROW -> {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            calendar.time
        }
        Day.YESTERDAY -> {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            calendar.time
        }
    }
}

fun Double.format(digits: Int) = String.Companion.format(
    java.util.Locale.GERMAN,
    "%.${digits}f",
    this
)

fun MutableList<Rate>.getCurrency(code: Int): Rate? {
    for (rate in this) {
        if (rate.code == code) return rate
    }
    return null
}

fun MutableList<Rate>.getCurrencies(codes: List<Int>): List<Rate?> {
    val listRate = mutableListOf<Rate>()
    for (rate in this) {
        for (code in codes) {
            if (rate.code == code) {
                listRate.add(rate)
                break
            }
        }
        if (listRate.size == codes.size) break
    }
    return listRate
}

fun MutableList<Rate>.getAllCodes(): List<Int?> {
    val listCodes = mutableListOf<Int>()
    for (rate in this) {
        listCodes.add(rate.code)
    }
    return listCodes
}

fun MutableList<Rate>.changeState(codes: List<Int>, state: Boolean) {
    for (rate in this) {
        for (code in codes) {
            if (rate.code == code) {
                rate.isChecked = state
                break
            }
        }
    }
}

fun MutableList<Rate>.changeState(rates: List<Rate>) {
    for (rate in this) {
        for (currency in rates) {
            if (rate.code == currency.code) {
                rate.isChecked = currency.isChecked
                break
            }
        }
    }
}

fun MutableList<Rate>.changeState(code: Int, state: Boolean) {
    for (rate in this) {
        if (rate.code == code) {
            rate.isChecked = state
            break
        }
    }
}

fun MutableList<Rate>.changeState(codes: Map<Int, Boolean>) {
    for (rate in this) {
        for (currency in codes)
            if (rate.code == currency.key) {
                rate.isChecked = currency.value
                break
            }
    }
}

fun MutableList<Rate>.removeRateAtCode(code: Int) {
    var shouldRemove = false
    var rateForRemove: Rate? = null
    for (rate in this) {
        if (rate.code == code) {
            shouldRemove = true
            rateForRemove = rate
            break
        }
    }
    if (shouldRemove) this.remove(rateForRemove)
}

enum class Day {
    TODAY,
    TOMORROW,
    YESTERDAY
}



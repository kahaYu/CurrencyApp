package com.raywenderlich.currencyapp.ui.customRefreshLayout

import android.content.Context
import android.util.AttributeSet
import android.view.View

abstract class BaseProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var mParent: IGRefreshLayout? = null
    var mPercent = 0f
    var isLoading = false

    abstract fun setPercent(percent: Float)
    abstract fun setParent(parent: IGRefreshLayout)
    abstract fun start()
    abstract fun stop()

    fun dp2px(dp: Int): Int{
        return dp*context.resources.displayMetrics.density.toInt()
    }
}
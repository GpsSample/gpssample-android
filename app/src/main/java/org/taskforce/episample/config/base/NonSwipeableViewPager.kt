package org.taskforce.episample.config.base

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent

class NonSwipeableViewPager(context: Context, attributeSet: AttributeSet? = null):
        ViewPager(context, attributeSet) {

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = false

    override fun onTouchEvent(ev: MotionEvent?): Boolean = false

    override fun executeKeyEvent(event: KeyEvent): Boolean = false
}
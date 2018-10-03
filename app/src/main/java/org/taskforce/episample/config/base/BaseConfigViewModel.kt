package org.taskforce.episample.config.base

import android.databinding.ObservableField
import android.view.View

interface BaseConfigViewModel {

    val backEnabled: ObservableField<Boolean>
    val nextEnabled: ObservableField<Boolean>

    val backText: String
        get() = "BACK"
    val nextText: String
        get() = "NEXT"
    val progress: Int
        get() = 0
    val configScreenCount: Int
        get() = 10

    fun onNextClicked(view: View)
    fun onBackClicked(view: View)
}
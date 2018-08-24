package org.taskforce.episample.collection.ui

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableField

class CollectGpsPrecisionViewModel(private val minimumPrecision: Double,
                                   private val preferredPrecision: Double,
                                   private val lowestColor: Int,
                                   private val mediumColor: Int,
                                   private val highestColor: Int) : BaseObservable() {

    val precision = ObservableField<Double>(0.0)

    @get:Bindable
    val displayValue = object : ObservableField<String>(precision) {
        override fun get() = String.format("%.1f m", precision.get())
    }

    @get:Bindable
    val displayColor = object : ObservableField<Int>(precision) {
        override fun get(): Int? {
            val color = precision.get()?.let {
                when {
                    it > minimumPrecision -> lowestColor
                    it > preferredPrecision -> mediumColor
                    else -> highestColor
                }
            } ?: lowestColor
            return color
        }
    }
    
    @get:Bindable
    val gps1Enabled = ObservableField(true)

    @get:Bindable
    val gps2Enabled = object : ObservableField<Boolean>(precision) {
        override fun get() = precision.get()?.let { it <= minimumPrecision }
    }

    @get:Bindable
    val gps3Enabled = object : ObservableField<Boolean>(precision) {
        override fun get() = precision.get()?.let { it <= preferredPrecision }
    }

}
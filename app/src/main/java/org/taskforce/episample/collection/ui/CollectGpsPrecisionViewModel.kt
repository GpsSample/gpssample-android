package org.taskforce.episample.collection.ui

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField

class CollectGpsPrecisionViewModel(private val minimumPrecision: Double,
                                   private val preferredPrecision: Double,
                                   private val lowestColor: Int,
                                   private val mediumColor: Int,
                                   private val highestColor: Int) : ViewModel() {

    val precision = ObservableField<Double>(0.0)

    val displayValue = object : ObservableField<String>(precision) {
        override fun get() = String.format("%.1f m", precision.get())
    }

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
    
    val gps1Enabled = ObservableField(true)

    val gps2Enabled = object : ObservableField<Boolean>(precision) {
        override fun get() = precision.get()?.let { it <= minimumPrecision }
    }

    val gps3Enabled = object : ObservableField<Boolean>(precision) {
        override fun get() = precision.get()?.let { it <= preferredPrecision }
    }

}
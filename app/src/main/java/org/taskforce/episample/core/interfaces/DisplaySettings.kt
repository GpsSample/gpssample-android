package org.taskforce.episample.core.interfaces

import java.text.SimpleDateFormat
import java.util.*

interface DisplaySettings {
    val isMetricDate: Boolean
    val is24HourTime: Boolean
    
    fun getDateFormat(): String = if (isMetricDate) {
        "dd/MM/yy"
    } else {
        "MM/dd/yy"
    }

    fun getTimeFormat(): String = if (is24HourTime) {
        "kk:mm"
    } else {
        "K:mm a"
    }

    fun getFormattedDateWithTime(date: Date, isLongFormDate: Boolean): String {
        return "${getFormattedDate(date, isLongFormDate)} ${getFormattedTime(date)}"
    }

    fun getFormattedDate(date: Date, longForm: Boolean): String {
        val calendar = Calendar.getInstance()
        calendar.time = date

        val month = calendar.get(Calendar.MONTH) + 1

        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val year = calendar.get(Calendar.YEAR)

        return if (longForm) {
            if (isMetricDate) {
                "$day ${calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())}, $year"
            } else {
                "${calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} $day, $year"
            }
        } else {
            if (isMetricDate) {
                "$day/$month/$year"
            } else {
                
                "$month/$day/$year"
            }
        }
    }

    fun getFormattedTime(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return SimpleDateFormat(getTimeFormat()).format(date)
    }

    companion object {
        val default = object : DisplaySettings {
            override val isMetricDate: Boolean
                get() = true
            override val is24HourTime: Boolean
                get() = false //To change initializer of created properties use File | Settings | File Templates.
        }
    }
}
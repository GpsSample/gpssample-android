package org.taskforce.episample.core.interfaces

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
        "KK:mm"
    }
}
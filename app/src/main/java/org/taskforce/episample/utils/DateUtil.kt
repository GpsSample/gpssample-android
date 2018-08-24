package org.taskforce.episample.utils

import org.taskforce.episample.core.interfaces.DisplaySettings
import java.text.SimpleDateFormat
import java.util.*

class DateUtil {
    companion object {
        fun getFormattedDate(date: Date, displaySettings: DisplaySettings?): String {
            return SimpleDateFormat(displaySettings?.getDateFormat()).format(date)
        }

        fun getFormattedTime(date: Date, displaySettings: DisplaySettings?): String {
            return SimpleDateFormat(displaySettings?.getTimeFormat()).format(date)
        }
    }
}
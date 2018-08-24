package org.taskforce.episample.config.settings.display

import java.io.Serializable
import java.util.*

class DisplaySettings(private var isDateMetric: Boolean = false,
                      private var isTime24Hour: Boolean = false) : Serializable {


    fun getFormattedDate(date: Date, longForm: Boolean): String {
        val calendar = Calendar.getInstance()
        calendar.time = date

        return if (longForm) {
            if (isDateMetric) {
                "${calendar.get(Calendar.DAY_OF_MONTH)} ${calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())}, ${calendar.get(Calendar.YEAR)}"
            } else {
                "${calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} ${calendar.get(Calendar.DAY_OF_MONTH)}, ${calendar.get(Calendar.YEAR)}"
            }
        } else {
            if (isDateMetric) {
                "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH)}/${calendar.get(Calendar.YEAR)}"
            } else {
                "${calendar.get(Calendar.MONTH)}/${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.YEAR)}"
            }
        }
    }

    fun getFormattedTime(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date

        return if (isTime24Hour) {
            "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
        }
        else {
            "${calendar.get(Calendar.HOUR)}:${calendar.get(Calendar.MINUTE)} ${calendar.get(Calendar.AM_PM)}"
        }
    }
}
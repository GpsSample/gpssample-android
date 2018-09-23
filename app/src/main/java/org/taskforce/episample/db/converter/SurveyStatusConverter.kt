package org.taskforce.episample.db.converter

import android.arch.persistence.room.TypeConverter
import org.taskforce.episample.core.navigation.SurveyStatus

class SurveyStatusConverter {

    @TypeConverter
    fun fromString(value: String?): SurveyStatus? {
        value?.let {
            when {
                it == "complete" -> return SurveyStatus.Complete()
                it == "incomplete" -> return SurveyStatus.Incomplete()
                it.startsWith("skipped-") -> return SurveyStatus.Skipped(it.substring(8))
                it.startsWith("problem-") -> return SurveyStatus.Problem(it.substring(8))
                else -> {
                    return null
                }
            }
        } ?: return null
    }

    @TypeConverter
    fun toString(status: SurveyStatus?): String? {
        return when (status) {
            is SurveyStatus.Complete -> return "complete"
            is SurveyStatus.Incomplete -> return "incomplete"
            is SurveyStatus.Skipped -> return "skipped-${status.reason}"
            is SurveyStatus.Problem -> return "problem-${status.reason}"
            else -> {
                return null
            }
        }
    }
}
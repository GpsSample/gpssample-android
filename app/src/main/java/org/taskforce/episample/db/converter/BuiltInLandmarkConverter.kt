package org.taskforce.episample.db.converter

import android.arch.persistence.room.TypeConverter
import android.util.Log
import org.taskforce.episample.core.BuiltInLandmark

class BuiltInLandmarkConverter {

    @TypeConverter
    fun fromName(value: String?): BuiltInLandmark? {
        return BuiltInLandmark.values().firstOrNull { it.name == value }
    }

    @TypeConverter
    fun typeToName(type: BuiltInLandmark?): String? {
        return type?.name
    }

}

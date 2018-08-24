package org.taskforce.episample.db.converter

import android.arch.persistence.room.TypeConverter
import org.taskforce.episample.db.config.customfield.CustomFieldType

class CustomFieldTypeConverter {

    @TypeConverter
    fun fromName(value: String?): CustomFieldType? {
        return if (value == null) null else {
            CustomFieldType.values().firstOrNull { it.name == value }
        }
    }

    @TypeConverter
    fun typeToName(type: CustomFieldType?): String? {
        return if (type == null) null else type.name
    }
}

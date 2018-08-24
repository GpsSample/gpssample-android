package org.taskforce.episample.db.converter

import android.arch.persistence.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.taskforce.episample.core.util.GsonUtil
import org.taskforce.episample.db.config.customfield.value.*
import java.io.IOException

class CustomFieldValueTypeConverter {

    @TypeConverter
    fun fromJsonString(value: String?): CustomFieldValueType? {
        return if (value == null) null else {
            val valueTypes = listOf(
                    BooleanValue::class.java,
                    DateValue::class.java,
                    DoubleValue::class.java,
                    DropdownValue::class.java,
                    IntValue::class.java,
                    TextValue::class.java
            )
            val mapper = jacksonObjectMapper()

            for (valueType in valueTypes) {
                try {
                    return mapper.readValue(value, valueType) as CustomFieldValueType
                } catch(e: Exception) {
                    // no-op
                }
            }

            throw IOException("JSON not a registered CustomFieldValueType")
        }
    }

    @TypeConverter
    fun valueToJson(type: CustomFieldValueType?): String? {
        return if (type == null) null else {
            GsonUtil.getDefaultBuilder()
                    .create()
                    .toJson(type)
        }
    }
}
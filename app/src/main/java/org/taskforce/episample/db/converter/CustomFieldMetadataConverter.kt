package org.taskforce.episample.db.converter

import android.arch.persistence.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.taskforce.episample.core.util.GsonUtil
import org.taskforce.episample.db.config.customfield.metadata.*

class CustomFieldMetadataConverter {

    @TypeConverter
    fun fromJsonString(value: String?): CustomFieldMetadata? {
        return if (value == null) null else {
            val metadataTypes = listOf(
                    DateMetadata::class.java,
                    DropdownMetadata::class.java,
                    NumberMetadata::class.java
            )
            val mapper = jacksonObjectMapper()

            for (metadataType in metadataTypes) {
                try {
                    return mapper.readValue(value, metadataType) as CustomFieldMetadata
                } catch(e: Exception) {
                    // no-op
                }
            }

            return EmptyMetadata()
        }
    }

    @TypeConverter
    fun metadataToJson(type: CustomFieldMetadata?): String? {
        return if (type == null) null else {
            GsonUtil.getDefaultBuilder()
                    .create()
                    .toJson(type)
        }
    }
}
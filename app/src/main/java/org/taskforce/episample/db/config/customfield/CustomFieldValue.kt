package org.taskforce.episample.db.config.customfield

import android.arch.persistence.room.*
import org.taskforce.episample.core.interfaces.CustomFieldValue
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.config.customfield.value.CustomFieldValueType
import org.taskforce.episample.db.converter.CustomFieldTypeConverter
import org.taskforce.episample.db.converter.CustomFieldValueTypeConverter
import java.util.*

@Entity(tableName = "custom_field_value_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Enumeration::class, parentColumns = ["id"], childColumns = ["enumeration_id"], onDelete = ForeignKey.CASCADE
            )),
            (ForeignKey(
                    entity = CustomField::class, parentColumns = ["id"], childColumns = ["custom_field_id"], onDelete = ForeignKey.CASCADE
            ))
        ])


@TypeConverters(
        CustomFieldTypeConverter::class,
        CustomFieldValueTypeConverter::class
)
class CustomFieldValue(
        override val value: CustomFieldValueType,
        override val type: CustomFieldType,
        @ColumnInfo(name = "enumeration_id")
        val enumerationId: String,
        @ColumnInfo(name = "custom_field_id")
        override val customFieldId: String,
        @PrimaryKey
        val id: String = UUID.randomUUID().toString()
): CustomFieldValue {

    companion object {
        fun makeDBCustomFieldValue(customFieldValue: CustomFieldValue, enumerationId: String): org.taskforce.episample.db.config.customfield.CustomFieldValue = CustomFieldValue(customFieldValue.value,
                customFieldValue.type,
                enumerationId,
                customFieldValue.customFieldId)
    }
}
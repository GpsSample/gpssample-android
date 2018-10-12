package org.taskforce.episample.core.interfaces

import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.value.*
import org.taskforce.episample.utils.DateUtil

interface CustomFieldValue {
    val value: CustomFieldValueType
    val type: CustomFieldType
    val customFieldId: String
}

data class LiveCustomFieldValue(override val value: CustomFieldValueType,
                                override val type: CustomFieldType,
                                override val customFieldId: String) : CustomFieldValue

fun CustomFieldValue.getValueForCustomField(customField: CustomField, displaySettings: DisplaySettings): String {
    val cfv = this.value
    return when (cfv) {
        is BooleanValue -> {
            cfv.boolValue.toString()
        }
        is DateValue -> {
            val date = cfv.dateValue
            DateUtil.getFormattedDate(date, displaySettings)
        }
        is DoubleValue -> {
            cfv.doubleValue.toString()
        }
        is IntValue -> {
            cfv.intValue.toString()
        }
        is TextValue -> {
            cfv.text
        }
        is DropdownValue -> {
            val dropdownMetadata = customField.metadata as org.taskforce.episample.db.config.customfield.metadata.DropdownMetadata
            val dropdownItem = dropdownMetadata.items.first { it.key == cfv.customDropdownId }
            dropdownItem.value ?: ""
        }
        else -> {
            ""
        }
    }
}
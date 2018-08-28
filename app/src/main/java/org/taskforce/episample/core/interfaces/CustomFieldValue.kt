package org.taskforce.episample.core.interfaces

import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.value.CustomFieldValueType

interface CustomFieldValue {
    val value: CustomFieldValueType
    val type: CustomFieldType
    val customFieldId: String
}

data class LiveCustomFieldValue(override val value: CustomFieldValueType,
                                override val type: CustomFieldType,
                                override val customFieldId: String) : CustomFieldValue
package org.taskforce.episample.config.fields

import org.taskforce.episample.db.config.customfield.CustomFieldType
import java.io.Serializable
import java.util.*

data class CustomField(
        val isAutomatic: Boolean,
        val isPrimary: Boolean,
        var isExported: Boolean,
        val isRequired: Boolean,
        val isPersonallyIdentifiableInformation: Boolean,
        val name: String,
        val type: CustomFieldType,
        val properties: Map<String, Any> = mapOf(),
        val customKey: String = UUID.randomUUID().toString()) : Serializable {
}

class CustomFieldDataItem(val type: CustomFieldType,
                          val isPrimary: Boolean,
                          var data: Any,
                          val id: String = UUID.randomUUID().toString()): Serializable

class CustomDropdown(val value: String?,
                          val key: String = UUID.randomUUID().toString()): Serializable

class CustomFieldTypeConstants {
    companion object {
        const val INTEGER_ONLY = "INTEGER_ONLY_KEY"
        const val DROPDOWN_ITEMS = "DROPDOWN_ITEMS"
        const val YEAR = "YEAR_KEY"
        const val MONTH = "MONTH_KEY"
        const val DAY = "DAY_KEY"
        const val TIME = "TIME_KEY"
    }
}
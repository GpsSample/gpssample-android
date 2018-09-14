package org.taskforce.episample.config.fields

import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.db.config.customfield.CustomDateType
import org.taskforce.episample.db.config.customfield.CustomFieldType

class CustomFieldDisplayFactory(private val languageService: LanguageService) {

    fun buildDisplayName(customField: CustomField) =
            if (customField.isRequired) {
                "${customField.name} *"
            } else {
                customField.name
            }

    fun buildDescription(customField: CustomField) =
            when (customField.type) {
                CustomFieldType.CHECKBOX -> languageService.getString(R.string.custom_checkbox)
                CustomFieldType.DATE -> generateDateDescription(customField)
                CustomFieldType.DROPDOWN -> {
                    val dropdownDescriptor =
                            if (customField.properties[CustomFieldTypeConstants.DROPDOWN_ITEMS] is List<*>) {
                                "(${(customField.properties[CustomFieldTypeConstants.DROPDOWN_ITEMS] as List<*>).size})"
                            } else {
                                ""
                            }
                    if (customField.properties[CustomFieldTypeConstants.DROPDOWN_ITEMS] is List<*>) {
                        "${languageService.getString(R.string.custom_dropdown)} $dropdownDescriptor"
                    } else {
                        languageService.getString(R.string.custom_dropdown)
                    }
                }
                CustomFieldType.NUMBER -> {
                    val numberDescriptor = if (customField.properties[CustomFieldTypeConstants.INTEGER_ONLY] == true) {
                        "(${languageService.getString(R.string.custom_number_integer)})"
                    } else {
                        ""
                    }
                    "${languageService.getString(R.string.custom_number)} $numberDescriptor"
                }
                CustomFieldType.TEXT -> languageService.getString(R.string.custom_text)
            }.removeSuffix(", ")

    private fun generateDateDescription(customField: CustomField): String {
        return when (customField.properties[CustomFieldTypeConstants.DATE] as CustomDateType) {
            CustomDateType.DATE -> languageService.getString(R.string.config_fields_add_date_date)
            CustomDateType.TIME -> languageService.getString(R.string.config_fields_add_date_date_time)
            CustomDateType.DATE_TIME -> languageService.getString(R.string.config_fields_add_date_time)
        }
    }
}
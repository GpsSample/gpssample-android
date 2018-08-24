package org.taskforce.episample.config.fields

class CustomFieldItemViewModel(
        isAutomatic: Boolean,
        val name: String,
        val description: String,
        customKey: String) {

    val displayEdit = !isAutomatic

    val displayDelete = !isAutomatic

    val displayCustomKey = customKey.isNotBlank()
}
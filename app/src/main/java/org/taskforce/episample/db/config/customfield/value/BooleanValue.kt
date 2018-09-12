package org.taskforce.episample.db.config.customfield.value

import com.fasterxml.jackson.annotation.JsonValue

data class BooleanValue(
        @JsonValue
        val boolValue: Boolean) : CustomFieldValueType
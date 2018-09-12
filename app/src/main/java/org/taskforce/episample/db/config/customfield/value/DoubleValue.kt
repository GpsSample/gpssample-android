package org.taskforce.episample.db.config.customfield.value

import com.fasterxml.jackson.annotation.JsonValue

data class DoubleValue(
        @JsonValue
        val doubleValue: Double): CustomFieldValueType
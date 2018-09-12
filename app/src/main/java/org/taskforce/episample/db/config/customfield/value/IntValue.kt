package org.taskforce.episample.db.config.customfield.value

import com.fasterxml.jackson.annotation.JsonValue

data class IntValue(
        @JsonValue
        var intValue: Int) : CustomFieldValueType
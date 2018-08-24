package org.taskforce.episample.db.config.customfield.metadata

import com.fasterxml.jackson.annotation.JsonProperty

data class NumberMetadata(
        @JsonProperty("isIntegerOnly")
        val isIntegerOnly: Boolean
) : CustomFieldMetadata
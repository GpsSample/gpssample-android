package org.taskforce.episample.db.config.customfield.metadata

import org.taskforce.episample.db.config.customfield.CustomDateType

data class DateMetadata(val dateType: CustomDateType, val useCurrentTime: Boolean) : CustomFieldMetadata
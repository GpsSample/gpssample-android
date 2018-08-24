package org.taskforce.episample.db.config.customfield.value

import com.fasterxml.jackson.annotation.JsonFormat
import org.taskforce.episample.core.util.GsonUtil
import java.util.*

class DateValue(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = GsonUtil.DATE_FORMAT)
        val dateValue: Date
): CustomFieldValueType
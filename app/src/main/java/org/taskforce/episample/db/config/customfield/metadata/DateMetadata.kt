package org.taskforce.episample.db.config.customfield.metadata

data class DateMetadata(val showYear: Boolean,
                        val showMonth: Boolean,
                        val showDay: Boolean,
                        val showTime: Boolean) : CustomFieldMetadata
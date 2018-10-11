package org.taskforce.episample.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.TypeConverters
import org.taskforce.episample.db.converter.DateConverter
import java.util.*

@TypeConverters(DateConverter::class)
class DateRange(
        @ColumnInfo(name = "min_date")
        var minimumDate: Date?,
        @ColumnInfo(name = "max_date")
        var maximumDate: Date?
)
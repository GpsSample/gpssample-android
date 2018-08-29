package org.taskforce.episample.db.config

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import org.taskforce.episample.config.settings.display.DisplaySettings
import org.taskforce.episample.db.converter.DateConverter
import java.util.*

@Entity(tableName = "study_table")
@TypeConverters(DateConverter::class)
class Study(
        val name: String,
        val password: String,
        @ColumnInfo(name = "date_created")
        val dateCreated: Date = Date(),
        @PrimaryKey()
        val id: String = UUID.randomUUID().toString()) {

        val dateCreatedDisplay
                get() = DisplaySettings().getFormattedDate(dateCreated, true)
}


package org.taskforce.episample.db.config

import android.arch.persistence.room.*
import org.taskforce.episample.config.settings.display.DisplaySettings
import org.taskforce.episample.db.converter.DateConverter
import java.util.*

@Entity(tableName = "study_table",
        foreignKeys =[
                (ForeignKey(
                        entity = Config::class, parentColumns = ["id"], childColumns = ["config_id"], onDelete = ForeignKey.CASCADE
                ))
        ])

@TypeConverters(DateConverter::class)
class Study(
        val name: String,
        val password: String,
        @ColumnInfo(name = "date_created")
        val dateCreated: Date = Date(),
        @ColumnInfo(name = "config_id")
        val configId: String,
        @PrimaryKey()
        val id: String = UUID.randomUUID().toString()) {

        val dateCreatedDisplay
                get() = DisplaySettings().getFormattedDate(dateCreated, true)
}


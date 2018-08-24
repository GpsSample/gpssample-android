package org.taskforce.episample.db.config

import android.arch.persistence.room.*
import org.taskforce.episample.db.converter.DateConverter
import java.util.*
@Entity(tableName = "config_table",
        foreignKeys =[
            (ForeignKey(
                    entity = Study::class, parentColumns = ["id"], childColumns = ["study_id"], onDelete = ForeignKey.SET_NULL
            ))
                ])

@TypeConverters(DateConverter::class)
class Config(
    var name: String,
    @ColumnInfo(name = "date_created")
    val dateCreated: Date = Date(),
    @PrimaryKey()
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "study_id", index = true)
    var studyId: String? = null)

package org.taskforce.episample.db.config

import android.arch.persistence.room.*
import org.taskforce.episample.db.converter.DateConverter
import java.util.*
@Entity(tableName = "config_table")

@TypeConverters(DateConverter::class)
class Config(
    var name: String,
    @ColumnInfo(name = "date_created")
    val dateCreated: Date = Date(),
    @PrimaryKey()
    val id: String = UUID.randomUUID().toString())

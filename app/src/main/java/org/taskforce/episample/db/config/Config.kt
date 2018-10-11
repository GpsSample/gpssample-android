package org.taskforce.episample.db.config

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import org.taskforce.episample.db.converter.DateConverter
import java.util.*

@Entity(tableName = "config_table")

@TypeConverters(
        DateConverter::class
)
class Config(
        var name: String,
        @ColumnInfo(name = "mapbox_style")
        val mapboxStyle: String,
        @ColumnInfo(name = "map_min_zoom")
        val mapMinZoom: Double,
        @ColumnInfo(name = "map_max_zoom")
        val mapMaxZoom: Double,
        @ColumnInfo(name = "date_created")
        val dateCreated: Date = Date(),
        @PrimaryKey()
        val id: String = UUID.randomUUID().toString())

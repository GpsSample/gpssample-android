package org.taskforce.episample.db.config

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "enumeration_area_point_table",
        foreignKeys = [
            (ForeignKey(entity = EnumerationArea::class, parentColumns = ["id"], childColumns = ["enumeration_area_id"], onDelete = ForeignKey.CASCADE))
        ])

class EnumerationAreaPoint(val lat: Double,
                           val lng: Double,
                           @ColumnInfo(name = "enumeration_area_id")
                           var enumerationAreaId: String,
                           @PrimaryKey
                           var id: String = UUID.randomUUID().toString())
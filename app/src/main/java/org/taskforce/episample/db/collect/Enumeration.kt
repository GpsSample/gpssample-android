package org.taskforce.episample.db.collect

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import org.taskforce.episample.db.config.Study
import org.taskforce.episample.db.converter.DateConverter
import java.util.Date
import java.util.UUID

@Entity(tableName = "enumeration_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Study::class, parentColumns = ["id"], childColumns = ["study_id"], onDelete = ForeignKey.CASCADE
            ))
        ])

@TypeConverters(DateConverter::class)
class Enumeration(
        @ColumnInfo(name = "collector_name")
        var collectorName: String,
        val lat: Double,
        val lng: Double,
        val note: String?,
        @ColumnInfo(name = "is_complete")
        val isIncomplete: Boolean,
        @ColumnInfo(name = "is_excluded")
        val isExcluded: Boolean,
        @ColumnInfo(name = "gps_precision")
        val gpsPrecision: Double,
        @ColumnInfo(name = "study_id")
        var studyId: String,
        val title: String? = null,
        val image: String? = null,
        @ColumnInfo(name = "date_created")
        val dateCreated: Date = Date(),
        @ColumnInfo(name = "incomplete_reason")
        val incompleteReason: String? = null,
        @PrimaryKey
        var id: String = UUID.randomUUID().toString(),
        @ColumnInfo(name = "is_deleted")
        var isDeleted: Boolean = false)
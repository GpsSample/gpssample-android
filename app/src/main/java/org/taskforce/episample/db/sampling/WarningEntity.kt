package org.taskforce.episample.db.sampling

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import java.util.*


@Entity(tableName = "sample_warnings",
        foreignKeys = [
            (android.arch.persistence.room.ForeignKey(
                    entity = SampleEntity::class, parentColumns = ["id"], childColumns = ["sample_id"], onDelete = ForeignKey.CASCADE
            ))
        ])
class WarningEntity(
        @ColumnInfo(name = "sample_id")
        var sampleId: String,
        var warning: String,
        @PrimaryKey
        var id: String = UUID.randomUUID().toString()
)
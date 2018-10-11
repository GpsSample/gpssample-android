package org.taskforce.episample.db.sampling

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import org.taskforce.episample.db.converter.DateConverter
import java.util.*

@Entity(tableName = "samples",
        foreignKeys = [
            (android.arch.persistence.room.ForeignKey(
                    entity = org.taskforce.episample.db.config.Study::class, parentColumns = ["id"], childColumns = ["study_id"]
            ))
        ])
@TypeConverters(DateConverter::class)
class SampleEntity(
        @ColumnInfo(name = "study_id")
        var studyId: String,
        @ColumnInfo(name = "date_created")
        var dateCreated: Date = Date(),
        @PrimaryKey
        var id: String = UUID.randomUUID().toString()
)
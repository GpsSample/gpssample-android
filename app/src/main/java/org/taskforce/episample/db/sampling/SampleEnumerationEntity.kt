package org.taskforce.episample.db.sampling

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import org.taskforce.episample.db.collect.Enumeration
import java.util.*


@Entity(tableName = "sample_enumerations",
        foreignKeys = [
            (android.arch.persistence.room.ForeignKey(
                    entity = SampleEntity::class, parentColumns = ["id"], childColumns = ["sample_id"], onDelete = ForeignKey.CASCADE
            )),
            (android.arch.persistence.room.ForeignKey(
                    entity = Enumeration::class, parentColumns = ["id"], childColumns = ["enumeration_id"]
            ))

        ])
class SampleEnumerationEntity(
        @ColumnInfo(name = "sample_id")
        var sampleId: String,
        @ColumnInfo(name = "enumeration_id")
        var enumerationId: String,
        @PrimaryKey
        var id: String = UUID.randomUUID().toString()
)
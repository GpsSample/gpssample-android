package org.taskforce.episample.db.config.landmark

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import org.taskforce.episample.core.interfaces.LandmarkType
import org.taskforce.episample.core.interfaces.LandmarkTypeMetadata
import org.taskforce.episample.db.config.Config
import java.util.*

@Entity(tableName = "custom_landmark_type_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Config::class, parentColumns = ["id"], childColumns = ["config_id"], onDelete = ForeignKey.CASCADE
            ))
        ])

class CustomLandmarkType(override val name: String,
                         override val iconLocation: String,
                         @ColumnInfo(name = "config_id")
                         var configId: String,
                         @PrimaryKey
                         var id: String = UUID.randomUUID().toString()): LandmarkType {
    override val metadata: LandmarkTypeMetadata
        get() = LandmarkTypeMetadata.CustomId(id)
}
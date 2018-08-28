package org.taskforce.episample.db.config

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import org.taskforce.episample.core.interfaces.UserSettings

@Entity(tableName = "user_settings_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Config::class, parentColumns = ["id"], childColumns = ["user_settings_config_id"], onDelete = ForeignKey.CASCADE
            ))
        ])

class UserSettings(
        @ColumnInfo(name = "user_settings_gps_minimum_precision")
        override val gpsMinimumPrecision: Double,
        @ColumnInfo(name = "user_settings_gps_preferred_precision")
        override val gpsPreferredPrecision: Double,
        override val allowPhotos: Boolean,
        @PrimaryKey
        @ColumnInfo(name = "user_settings_config_id")
        var configId: String) : UserSettings
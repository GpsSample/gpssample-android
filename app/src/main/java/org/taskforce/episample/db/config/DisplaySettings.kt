package org.taskforce.episample.db.config

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import org.taskforce.episample.core.interfaces.DisplaySettings

@Entity(tableName = "display_settings_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Config::class, parentColumns = ["id"], childColumns = ["display_settings_config_id"], onDelete = ForeignKey.CASCADE
            ))
        ])

class DisplaySettings(
        @ColumnInfo(name = "display_settings_is_metric_date")
        override val isMetricDate: Boolean,
        @ColumnInfo(name = "display_settings_is_military_time")
        override val is24HourTime: Boolean,
        @PrimaryKey
        @ColumnInfo(name = "display_settings_config_id")
        var configId: String) : DisplaySettings
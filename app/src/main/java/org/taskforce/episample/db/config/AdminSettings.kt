package org.taskforce.episample.db.config

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import org.taskforce.episample.core.interfaces.AdminSettings

@Entity(tableName = "admin_settings_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Config::class, parentColumns = ["id"], childColumns = ["admin_settings_config_id"], onDelete = ForeignKey.CASCADE
            ))
        ])

class AdminSettings(
        @ColumnInfo(name = "admin_settings_password")
        override var password: String,
        @PrimaryKey
        @ColumnInfo(name = "admin_settings_config_id")
        var configId: String): AdminSettings
package org.taskforce.episample.db.config

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "enumeration_subject_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Config::class, parentColumns = ["id"], childColumns = ["enumeration_subject_config_id"], onDelete = ForeignKey.CASCADE
            ))
        ])

class EnumerationSubject(
        @ColumnInfo(name = "enumeration_subject_singular")
        override val singular: String,
        @ColumnInfo(name = "enumeration_subject_plural")
        override val plural: String,
        @ColumnInfo(name = "enumeration_subject_primary_label")
        override val primaryLabel: String,
        @PrimaryKey
        @ColumnInfo(name = "enumeration_subject_config_id")
        var configId: String) : org.taskforce.episample.core.interfaces.EnumerationSubject {
}
package org.taskforce.episample.db.navigation

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import org.taskforce.episample.db.config.Study
import java.util.UUID

@Entity(tableName = "navigation_plan_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Study::class, parentColumns = ["id"], childColumns = ["study_id"], onDelete = ForeignKey.CASCADE
            ))
        ])
class NavigationPlan(
        @ColumnInfo(name = "study_id")
        val studyId: String,
        val title: String,
        @PrimaryKey
        val id: String = UUID.randomUUID().toString())
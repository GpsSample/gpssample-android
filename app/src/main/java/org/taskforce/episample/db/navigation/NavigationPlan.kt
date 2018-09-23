package org.taskforce.episample.db.navigation

import android.arch.persistence.room.*
import org.taskforce.episample.db.config.Study
import java.util.*

@Entity(tableName = "navigation_plan_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Study::class, parentColumns = ["id"], childColumns = ["study_id"]
            ))
        ])
class NavigationPlan(
        @ColumnInfo(name = "study_id")
        val studyId: String,
        val title: String,
        @PrimaryKey
        val id: String = UUID.randomUUID().toString())
package org.taskforce.episample.db.navigation

import android.arch.persistence.room.*
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.converter.DateConverter
import org.taskforce.episample.db.converter.SurveyStatusConverter
import java.util.*

@Entity(tableName = "navigation_item_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Enumeration::class, parentColumns = ["id"], childColumns = ["enumeration_id"]
            )),
            (ForeignKey(
                    entity = NavigationPlan::class, parentColumns = ["id"], childColumns = ["navigation_plan_id"], onDelete = ForeignKey.CASCADE
            ))
        ])
@TypeConverters(
        DateConverter::class,
        SurveyStatusConverter::class
)
class NavigationItem(
        @ColumnInfo(name = "navigation_plan_id")
        val navigationPlanId: String,
        @ColumnInfo(name = "enumeration_id")
        val enumerationId: String,
        @ColumnInfo(name = "navigation_order")
        val navigationOrder: Int,
        @ColumnInfo(name = "survey_status")
        var surveyStatus: SurveyStatus,
        @ColumnInfo(name = "date_created")
        val dateCreated: Date = Date(),
        @PrimaryKey
        val id: String = UUID.randomUUID().toString())

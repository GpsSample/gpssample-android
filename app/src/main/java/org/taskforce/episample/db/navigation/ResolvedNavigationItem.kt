package org.taskforce.episample.db.navigation

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Relation
import android.arch.persistence.room.TypeConverters
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.ResolvedEnumeration
import org.taskforce.episample.db.converter.DateConverter
import org.taskforce.episample.db.converter.SurveyStatusConverter
import java.util.*

@TypeConverters(
        DateConverter::class,
        SurveyStatusConverter::class
)
class ResolvedNavigationItem(
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
        val id: String
) {

    @Relation(entity = Enumeration::class, parentColumn = "enumeration_id", entityColumn = "id")
    lateinit var enumerations: List<ResolvedEnumeration>

    val enumeration: ResolvedEnumeration
        get() = enumerations.first()
}
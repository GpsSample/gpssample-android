package org.taskforce.episample.db.navigation

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Relation

class ResolvedNavigationPlan(
        @ColumnInfo(name = "study_id")
        val studyId: String,
        val title: String,
        val id: String) {

    @Relation(entity = NavigationItem::class, parentColumn = "id", entityColumn = "navigation_plan_id")
    lateinit var navigationItems: List<ResolvedNavigationItem>
}
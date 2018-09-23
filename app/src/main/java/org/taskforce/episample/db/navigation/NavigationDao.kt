package org.taskforce.episample.db.navigation

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.ResolvedEnumeration

@Dao
abstract class NavigationDao {

    @Insert
    abstract fun insertNavigationItem(navigationItem: NavigationItem)
    @Insert
    abstract fun insertNavigationPlan(navigationPlan: NavigationPlan)
    @Update
    abstract fun updateNavigationItem(navigationItem: NavigationItem)

    @Query("SELECT * FROM navigation_plan_table")
    abstract fun getAllNavigationPlans(): LiveData<List<ResolvedNavigationPlan>>

    @Query("SELECT * FROM navigation_plan_table")
    abstract fun getAllNavigationPlansSync(): List<ResolvedNavigationPlan>

//    @Query("SELECT * FROM navigation_item_table")
//    abstract fun getAllNavigationItems(): LiveData<List<ResolvedNavigationItem>>
//
//    @Query("SELECT * FROM navigation_item_table")
//    abstract fun getAllNavigationItemsSync(): List<ResolvedNavigationItem>


    @Query("SELECT * FROM navigation_plan_table WHERE id LIKE :id")
    abstract fun getNavigationPlan(id: String): LiveData<ResolvedNavigationPlan>

    @Query("SELECT * FROM navigation_plan_table WHERE id LIKE :id")
    abstract fun getNavigationPlanSync(id: String): ResolvedNavigationPlan

    @Query("SELECT * FROM navigation_item_table WHERE id LIKE :id")
    abstract fun getNavigationItemSync(id: String): NavigationItem

    @Transaction
    open fun updateNavigationItem(navigationItemId: String, surveyStatus: SurveyStatus) {
        val navigationItem = getNavigationItemSync(navigationItemId)
        navigationItem.surveyStatus = surveyStatus
        updateNavigationItem(navigationItem)
    }

    @Transaction
    open fun createDemoNavigationPlan(studyId: String, enumerations: List<ResolvedEnumeration>): String {
        val insertNavigationPlan = NavigationPlan(studyId, "Navigation Plan 1")
        insertNavigationPlan(insertNavigationPlan)

        enumerations.forEachIndexed({ index, item ->
            insertNavigationItem(NavigationItem(
                    navigationPlanId = insertNavigationPlan.id,
                    enumerationId = item.id,
                    navigationOrder = index,
                    surveyStatus = SurveyStatus.Incomplete()))
        })

        return insertNavigationPlan.id
    }
}
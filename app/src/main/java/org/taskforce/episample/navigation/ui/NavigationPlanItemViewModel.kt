package org.taskforce.episample.navigation.ui

import org.taskforce.episample.core.interfaces.NavigationPlan

class NavigationPlanItemViewModel(val planName: String,
                                  val planCount: String,
                                  private val planData: NavigationPlan,
                                  val viewPlan: (NavigationPlan) -> Unit) {

    fun viewNavigationPlan() {
        viewPlan(planData)
    }
}
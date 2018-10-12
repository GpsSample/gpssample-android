package org.taskforce.episample.navigation.ui

import org.taskforce.episample.core.interfaces.NavigationPlan

open class NavigationPlanItemViewModel(val planName: String,
                                       val planCount: String,
                                       private val planData: NavigationPlan?,
                                       private val viewPlan: ((NavigationPlan) -> Unit)?) {

    open fun viewNavigationPlan() {
        planData?.let { viewPlan?.invoke(it) }
    }
}
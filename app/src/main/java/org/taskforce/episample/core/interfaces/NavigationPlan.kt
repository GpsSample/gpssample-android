package org.taskforce.episample.core.interfaces

interface NavigationPlan {
    val studyId: String
    val title: String
    val id: String
    val navigationItems: List<NavigationItem>
}

data class LiveNavigationPlan(override val studyId: String,
                              override val title: String,
                              override val id: String,
                              override val navigationItems: List<NavigationItem>) : NavigationPlan
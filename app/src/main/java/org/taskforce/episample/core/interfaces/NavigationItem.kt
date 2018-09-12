package org.taskforce.episample.core.interfaces

import org.taskforce.episample.core.navigation.SurveyStatus

interface NavigationItem: Enumeration {
    override val title: String
    val navigationOrder: Int
    val surveyStatus: SurveyStatus
}

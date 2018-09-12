package org.taskforce.episample.core.navigation

sealed class SurveyStatus {
    class Incomplete(): SurveyStatus()
    class Complete(): SurveyStatus()
    class Problem(val reason: String): SurveyStatus()
    class Skipped(val reason: String): SurveyStatus()
}

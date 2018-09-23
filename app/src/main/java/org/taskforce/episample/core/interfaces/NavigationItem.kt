package org.taskforce.episample.core.interfaces

import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.navigation.SurveyStatus
import java.util.*

interface NavigationItem : Enumeration {
    override val title: String
    val navigationOrder: Int
    val surveyStatus: SurveyStatus
}

data class LiveNavigationItem(override val title: String,
                              override val navigationOrder: Int,
                              override val surveyStatus: SurveyStatus,
                              override val isIncomplete: Boolean,
                              override val customFieldValues: List<CustomFieldValue>,
                              override val isExcluded: Boolean,
                              override val id: String?,
                              override val note: String?,
                              override val image: String?,
                              override val location: LatLng,
                              override val gpsPrecision: Double,
                              override val dateCreated: Date) : NavigationItem
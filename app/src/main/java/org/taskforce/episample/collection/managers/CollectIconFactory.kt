package org.taskforce.episample.collection.managers

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.CollectItem
import org.taskforce.episample.core.interfaces.Enumeration
import org.taskforce.episample.core.interfaces.Landmark
import org.taskforce.episample.core.interfaces.NavigationItem
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.utils.getResourceUri
import java.io.File

interface IconFactory {
    fun getBitmapDescriptor(collectItem: CollectItem): BitmapDescriptor
    fun getIconUri(collectItem: CollectItem): String
}

class CollectIconFactory(
        private val resources: Resources) : IconFactory {

    override fun getBitmapDescriptor(collectItem: CollectItem): BitmapDescriptor =
            when (collectItem) {
                is Landmark -> {
                    if (collectItem.image == null) {
                        BitmapDescriptorFactory.fromResource(R.drawable.icon_landmark_default)
                    } else {
                        if (Uri.parse(collectItem.image).scheme == ContentResolver.SCHEME_ANDROID_RESOURCE) {
                            BitmapDescriptorFactory.fromResource(
                                    collectItem.image?.split(File.separator)?.last()?.toInt()
                                            ?: R.drawable.icon_landmark_default)
                        } else {
                            BitmapDescriptorFactory.fromPath(collectItem.image)
                        }
                    }
                }
                is NavigationItem -> {
                    when (collectItem.surveyStatus) {
                        is SurveyStatus.Incomplete -> BitmapDescriptorFactory.fromResource(R.drawable.household_todo)
                        is SurveyStatus.Complete,
                        is SurveyStatus.Problem -> BitmapDescriptorFactory.fromResource(R.drawable.household_completed)
                        is SurveyStatus.Skipped -> BitmapDescriptorFactory.fromResource(R.drawable.household_skipped)
                    }
                }
                is Enumeration -> BitmapDescriptorFactory.fromResource(R.drawable.household_todo)
                else -> {
                    throw IllegalArgumentException("CollectItem must either be LandmarkItem or EnumerationItem.")
                }
            }

    override fun getIconUri(collectItem: CollectItem) =
            when (collectItem) {
                is Landmark -> if (collectItem.image?.isNotBlank() == true) {
                    collectItem.image!!
                } else {
                    resources.getResourceUri(R.drawable.icon_landmark_default).toString()
                }
                is NavigationItem -> {
                    when (collectItem.surveyStatus) {
                        is SurveyStatus.Incomplete -> resources.getResourceUri(R.drawable.household_todo).toString()
                        is SurveyStatus.Complete,
                        is SurveyStatus.Problem -> resources.getResourceUri(R.drawable.household_completed).toString()
                        is SurveyStatus.Skipped -> resources.getResourceUri(R.drawable.household_skipped).toString()
                    }
                }
                is Enumeration -> {
                    resources.getResourceUri(R.drawable.household_todo).toString()
                }
                else -> {
                    throw IllegalArgumentException("CollectItem must either be LandmarkItem or EnumerationItem.")
                }
            }
}
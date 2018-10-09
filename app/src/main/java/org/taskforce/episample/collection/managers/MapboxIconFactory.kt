package org.taskforce.episample.collection.managers

import android.content.Context
import com.mapbox.mapboxsdk.annotations.Icon
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.core.navigation.SurveyStatus
class MapboxIconFactory {
    companion object {
        fun getMapboxIcon(context: Context, collectItem: CollectItem): Icon {

            val resourceId = when (collectItem) {
                is Landmark -> {
                    val metadata = collectItem.landmarkType.metadata
                    when (metadata) {
                        is LandmarkTypeMetadata.BuiltInLandmark -> {
                            metadata.type.drawableResource
                        }
                        else -> {
                            throw  IllegalStateException("Custom Landmarks are not supported")
                        }
                    }
                }
                is NavigationItem -> {
                    when (collectItem.surveyStatus) {
                        is SurveyStatus.Incomplete -> R.drawable.household_todo
                        is SurveyStatus.Complete,
                        is SurveyStatus.Problem -> R.drawable.household_completed
                        is SurveyStatus.Skipped -> R.drawable.household_skipped
                    }
                }
                is Enumeration -> R.drawable.household_todo
                else -> {
                    throw IllegalArgumentException("CollectItem must either be NavigationItem, LandmarkItem or EnumerationItem.")
                }
            }
            return getMapboxIcon(context, resourceId)
        }

        fun getMapboxIcon(context: Context, resourceId: Int): Icon {
            val factory = com.mapbox.mapboxsdk.annotations.IconFactory.getInstance(context)
            return factory.fromResource(resourceId)
        }
    }
}
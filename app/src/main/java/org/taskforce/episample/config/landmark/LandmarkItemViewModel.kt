package org.taskforce.episample.config.landmark

import android.view.View
import org.taskforce.episample.fileImport.models.LandmarkType

class LandmarkItemViewModel(val landmarkItem: LandmarkType,
                            val landmarkInteractionListener: LandmarkInteractionCallbacks) {

    val imageUrl = landmarkItem.iconLocation
    val name = landmarkItem.name

    fun onEditClicked(view: View) {
        landmarkInteractionListener.editLandmark(landmarkItem)
    }

    fun onDeleteClicked(view: View) {
        landmarkInteractionListener.deleteLandmark(landmarkItem)
    }

    interface LandmarkInteractionCallbacks {
        fun editLandmark(landmarkType: LandmarkType)
        fun deleteLandmark(landmarkType: LandmarkType)
    }
}
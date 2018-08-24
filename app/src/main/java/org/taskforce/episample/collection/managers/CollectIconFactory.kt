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
import org.taskforce.episample.utils.getResourceUri
import java.io.File


class CollectIconFactory(
        private val resources: Resources) {

    fun getBitmapDescriptor(collectItem: CollectItem): BitmapDescriptor =
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
                    
                    
                is Enumeration -> BitmapDescriptorFactory.fromResource(R.drawable.household_todo)
                else -> {
                    throw IllegalArgumentException("CollectItem must either be LandmarkItem or EnumerationItem.")
                }
            }

    fun getIconUriFromCollectItem(collectItem: CollectItem) =
            when (collectItem) {
                is Landmark -> if (collectItem.image?.isNotBlank() == true) {
                    collectItem.image
                } else {
                    resources.getResourceUri(R.drawable.icon_landmark_default).toString()
                }
                is Enumeration -> {
                    resources.getResourceUri(R.drawable.household_todo).toString()
                }
                else -> {
                    null
                }
            }
}
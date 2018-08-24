package org.taskforce.episample.collection.managers

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.taskforce.episample.core.interfaces.CollectItem

class CollectionItemMarkerManager(
        private val collectIconFactory: CollectIconFactory,
        private val googleMap: GoogleMap) {

    private val markers = mutableListOf<Pair<CollectItem, Marker>>()

    fun addMarker(collectItem: CollectItem) {
        val marker = googleMap.addMarker(
                MarkerOptions()
                        .position(collectItem.location)
                        .title(collectItem.title)
                        .icon(collectIconFactory.getBitmapDescriptor(collectItem))
        )
        marker.tag = collectItem
        
        markers.add(
                Pair(
                        collectItem,
                        marker
                )
        )
    }

    fun addMarkerDiff(inputMarkers: List<CollectItem>) {
        (inputMarkers - markers.map {
            it.first
        }).forEach {
            addMarker(it)
        }
    }

    fun removeAllMarkers() {
        markers.forEach {
            it.second.remove()
        }
    }
}
package org.taskforce.episample.core.interfaces

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import org.taskforce.episample.config.geography.model.Feature
import java.io.Serializable
import java.util.*

interface EnumerationArea {
    val name: String
    val points: List<Pair<Double, Double>>
    val configId: String
    val id: String

    fun isWithinArea(latLng: LatLng): Boolean {
        return PolyUtil.containsLocation(latLng, points.map { LatLng(it.first, it.second) }, false)
    }
}

class GeoJsonEnumerationArea(override val name: String,
                             override val points: List<Pair<Double, Double>>,
                             override val configId: String,
                             override val id: String) : EnumerationArea, Serializable {

    companion object {
        const val kmToLatitudeRatioConstant = 110.574
        const val kmToLongitudeRatioConstant = 111.320

        fun kmToLongitude(distance: Double, latitude: Double) = distance /
                (kmToLongitudeRatioConstant*Math.cos(Math.toRadians(latitude)))

        fun kmToLatitude(distance: Double) = distance / kmToLatitudeRatioConstant
        
        fun createFromFeature(feature: Feature): GeoJsonEnumerationArea {
            val name = feature.properties["name"] ?: ""
            val points = mutableListOf<Pair<Double, Double>>()
            feature.geometry.coordinates[0].forEach {
                val long = it[0]
                val lat = it[1]
                points.add(Pair(lat, long))
            }

            return GeoJsonEnumerationArea(name,
                    points,
                    "",
                    UUID.randomUUID().toString())
        }
        
        fun createFromQuickstart(lat: Double, long: Double, radius: Double): GeoJsonEnumerationArea {
            val northLatitude = lat + kmToLatitude(radius)
            val southLatitude = lat - kmToLatitude(radius)
            val northEastLongitude = long + kmToLongitude(radius, northLatitude)
            val southEastLongitude = long + kmToLongitude(radius, southLatitude)
            val northWestLongitude = long - kmToLongitude(radius, northLatitude)
            val southWestLongitude = long - kmToLongitude(radius, southLatitude)
            
            val quickstartName = "%.5f ".format(lat) +
                    ", %.5f".format(long) +
                    " (%.2f km)".format(radius)
            
            val points = listOf(
                    Pair(northLatitude, northWestLongitude),
                    Pair(northLatitude, northEastLongitude),
                    Pair(southLatitude, southEastLongitude),
                    Pair(southLatitude, southWestLongitude),
                    Pair(northLatitude, northWestLongitude)
            )
            
            return GeoJsonEnumerationArea(quickstartName, 
                    points, 
                    "", 
                    UUID.randomUUID().toString())
        }
    }

}
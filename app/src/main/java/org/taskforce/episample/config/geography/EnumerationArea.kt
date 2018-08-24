package org.taskforce.episample.config.geography

import com.google.android.gms.maps.model.LatLng

class EnumerationArea(val name: String,
                      val border: List<Pair<LatLng, LatLng>>) {

    val isValid = border.first() == border.last()

    companion object {

        private const val kmToLatitudeRatioConstant = 110.574
        private const val kmToLongitudeRatioConstant = 111.320

        fun kmToLongitude(distance: Double, latitude: Double) = distance /
                (kmToLongitudeRatioConstant*Math.cos(Math.toRadians(latitude)))

        fun kmToLatitude(distance: Double) = distance / kmToLatitudeRatioConstant

        fun quickstart(name: String, latitude: Double, longitude: Double, radius: Double): EnumerationArea {
            val northLatitude = latitude + kmToLatitude(radius)
            val southLatitude = latitude - kmToLatitude(radius)
            val northEastLongitude = longitude + kmToLongitude(radius, northLatitude)
            val southEastLongitude = longitude + kmToLongitude(radius, southLatitude)
            val northWestLongitude = longitude - kmToLongitude(radius, northLatitude)
            val southWestLongitude = longitude - kmToLongitude(radius, southLatitude)

            return EnumerationArea(name, listOf(
                    Pair(LatLng(northLatitude, northWestLongitude), LatLng(northLatitude, northEastLongitude)),
                    Pair(LatLng(northLatitude, northEastLongitude), LatLng(southEastLongitude, southEastLongitude)),
                    Pair(LatLng(southEastLongitude, southEastLongitude), LatLng(southLatitude, southWestLongitude)),
                    Pair(LatLng(southLatitude, southWestLongitude), LatLng(northLatitude, northWestLongitude))
            ))
        }
    }
}
package org.taskforce.episample.core

import android.content.Context
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.LandmarkType
import org.taskforce.episample.core.interfaces.LandmarkTypeMetadata
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.getResourceUri

enum class BuiltInLandmark {

    DEFAULT,
    APARTMENTS,
    BARN_SHED,
    BRIDGE,
    BUS_STOP,
    HOTEL,
    MARKET,
    MEDICAL,
    MEETING_PLACE,
    OFFICIAL,
    PARK,
    PETROL,
    RIVER_STREAM,
    SCHOOL,
    SHOP,
    TREE,
    WATER_SOURCE,
    WORSHIP;

    val drawableResource: Int
        get() {
            return when (this) {
                DEFAULT -> R.drawable.icon_landmark_default
                APARTMENTS -> R.drawable.apartments
                BARN_SHED -> R.drawable.barn_shed
                BRIDGE -> R.drawable.bridge
                BUS_STOP -> R.drawable.bus_stop
                HOTEL -> R.drawable.hotel
                MARKET -> R.drawable.market
                MEDICAL -> R.drawable.medical
                MEETING_PLACE -> R.drawable.meeting_place
                OFFICIAL -> R.drawable.official
                PARK -> R.drawable.park
                PETROL -> R.drawable.petrol
                RIVER_STREAM -> R.drawable.river_stream
                SCHOOL -> R.drawable.school
                SHOP -> R.drawable.shop
                TREE -> R.drawable.tree
                WATER_SOURCE -> R.drawable.water_source
                WORSHIP -> R.drawable.worship
            }
        }

    val titleResource: Int
        get() {
            return when (this) {
                DEFAULT -> LanguageManager.undefinedStringResourceId
                APARTMENTS -> R.string.landmark_apartments
                BARN_SHED -> R.string.landmark_barn_shed
                BRIDGE -> R.string.landmark_bridge
                BUS_STOP -> R.string.landmark_bus_stop
                HOTEL -> R.string.landmark_hotel
                MARKET -> R.string.landmark_market
                MEDICAL -> R.string.landmark_medical
                MEETING_PLACE -> R.string.landmark_meeting_place
                OFFICIAL -> R.string.landmark_official
                PARK -> R.string.landmark_park
                PETROL -> R.string.landmark_petrol
                RIVER_STREAM -> R.string.landmark_river_stream
                SCHOOL -> R.string.landmark_school
                SHOP -> R.string.landmark_shop
                TREE -> R.string.landmark_tree
                WATER_SOURCE -> R.string.landmark_water_source
                WORSHIP -> R.string.landmark_worship
            }
        }

    data class BuiltInLandmarkType(override val name: String,
                                   override val iconLocation: String,
                                   val sourceType: BuiltInLandmark) : LandmarkType {
        override val metadata: LandmarkTypeMetadata
            get() = LandmarkTypeMetadata.BuiltInLandmark(sourceType)
    }

    companion object {
        fun getLandmarkTypes(context: Context): List<BuiltInLandmarkType> {
            return BuiltInLandmark.values()
                    .filter { it != DEFAULT }
                    .map {
                        BuiltInLandmarkType(
                                context.getString(it.titleResource),
                                context.resources.getResourceUri(it.drawableResource).toString(),
                                it)
                    }
        }
    }
}
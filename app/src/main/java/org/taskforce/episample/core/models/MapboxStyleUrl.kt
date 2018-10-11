package org.taskforce.episample.core.models

import com.mapbox.mapboxsdk.constants.Style
import java.io.Serializable

class MapboxStyleUrl(val urlString: String): Serializable {
    companion object {
        const val DEFAULT_MAPBOX_STYLE = Style.MAPBOX_STREETS
        const val DEFAULT_MAX_ZOOM = 12.0
        const val DEFAULT_MIN_ZOOM = 8.0

    }
}

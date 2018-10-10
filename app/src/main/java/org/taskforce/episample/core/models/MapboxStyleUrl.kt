package org.taskforce.episample.core.models

import com.mapbox.mapboxsdk.constants.Style
import java.io.Serializable

class MapboxStyleUrl(val urlString: String): Serializable {
    companion object {
        const val DEFAULT_MAPBOX_STYLE = Style.MAPBOX_STREETS
    }
}

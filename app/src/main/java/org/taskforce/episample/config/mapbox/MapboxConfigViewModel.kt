package org.taskforce.episample.config.mapbox

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.support.v4.app.FragmentActivity
import android.view.View
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.offline.OfflineRegion
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition
import org.taskforce.episample.R
import org.taskforce.episample.config.base.BaseConfigViewModel
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.fields.CustomFieldsFragment
import org.taskforce.episample.core.models.MapboxStyleUrl

class MapboxConfigViewModel(private val configBuildManager: ConfigBuildManager): ViewModel(), BaseConfigViewModel {

    val offlineRegions = ObservableField<List<OfflineRegion>>(listOf())
    val styleUrl = ObservableField<String>(MapboxStyleUrl.DEFAULT_MAPBOX_STYLE)

    val isValid = object: ObservableField<Boolean>(styleUrl, offlineRegions) {
        override fun get(): Boolean? {
            offlineRegions.get()?.forEach {
                val bounds = (it.definition as OfflineTilePyramidRegionDefinition).bounds
                val downloadedStyleUrl = (it.definition as OfflineTilePyramidRegionDefinition).styleURL

                styleUrl.get().let {
                    if (it != downloadedStyleUrl) {
                        return false
                    }
                }

                var outlyingPointFound = false
                configBuildManager.config.enumerationAreas.forEach {
                    it.points.forEach {
                        if (!bounds.contains(LatLng(it.first, it.second))) {
                            outlyingPointFound = true
                        }
                    }
                }

                if (!outlyingPointFound) {
                    return true
                }
            }
            return false
        }
    }

    override val backEnabled = ObservableField<Boolean>(true)
    override val nextEnabled: ObservableField<Boolean> = object : ObservableField<Boolean>(isValid) {
        override fun get(): Boolean? {
            return isValid.get() ?: false
        }
    }

    val downloadEnabled = object: ObservableField<Boolean>(styleUrl, isValid) {
        override fun get(): Boolean? {
            return styleUrl.get()?.isBlank() == false && isValid.get() == false
        }
    }

    val testStyleEnabled = object: ObservableField<Boolean>() {
        override fun get(): Boolean? {
            return isValid.get() == false
        }
    }

    override fun onNextClicked(view: View) {
        styleUrl.get()?.let {
            configBuildManager.setMapboxStyle(MapboxStyleUrl(it))
            val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.configFrame, CustomFieldsFragment())
                    .addToBackStack(CustomFieldsFragment::class.qualifiedName)
                    .commit()
        }
    }

    override fun onBackClicked(view: View) {
        val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
        fragmentManager.popBackStack()
    }
}
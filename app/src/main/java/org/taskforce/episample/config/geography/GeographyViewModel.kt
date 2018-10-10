package org.taskforce.episample.config.geography

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.support.v4.app.FragmentActivity
import android.view.View
import org.taskforce.episample.R
import org.taskforce.episample.config.base.BaseConfigViewModel
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.fields.CustomFieldsFragment
import org.taskforce.episample.config.geography.model.FeatureCollection
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.mapbox.MapboxConfigFragment
import org.taskforce.episample.core.interfaces.GeoJsonEnumerationArea

class GeographyViewModel(
        val languageService: LanguageService,
        val adapter: EnumerationAreaAdapter,
        private val configBuildManager: ConfigBuildManager) :
        ViewModel(), QuickstartReceiver, OnDatasetChangedListener, BaseConfigViewModel {

    init {
        languageService.update = {
            quickstartButtonText.set(languageService.getString(R.string.config_geography_quickstart))
            quickstartExplanation.set(languageService.getString(R.string.config_geography_quickstart_explanation))
            viewMapText.set(languageService.getString(R.string.config_geography_quickstart_alternate))
            enumerationAreaTitle.set(languageService.getString(R.string.config_enumeration_area_title))
            enumerationAreaError.set(languageService.getString(R.string.config_list_empty))
        }
        adapter.onDatasetChangedListener = this
    }

    lateinit var quickstart: () -> Unit

    lateinit var pickFile: () -> Unit

    val quickstartButtonText = ObservableField(languageService.getString(R.string.config_geography_quickstart))

    val quickstartExplanation = ObservableField(languageService.getString(R.string.config_geography_quickstart_explanation))

    val itemCount = ObservableField(0)

    val quickstartVisibility = object : ObservableField<Boolean>(itemCount) {
        override fun get(): Boolean? = itemCount.get()!! == 0
    }

    val viewMapVisibility = object : ObservableField<Boolean>(quickstartVisibility) {
        override fun get(): Boolean? = itemCount.get()!! > 0
    }

    val viewMapText = ObservableField(languageService.getString(R.string.config_geography_quickstart_alternate))

    val filesHeader = ObservableField(languageService.getString(R.string.config_upload_title))

    val loadFileButtonText = ObservableField(languageService.getString(R.string.config_upload_load_file))

    val enumerationAreaTitle = object : ObservableField<String>(itemCount) {
        override fun get(): String? {
            return if (itemCount.get()!! > 0)
                "${languageService.getString(R.string.config_enumeration_area_title)} (${adapter.data?.size})"
            else
                languageService.getString(R.string.config_enumeration_area_title)
        }
    }

    val enumerationAreaError = ObservableField(languageService.getString(R.string.config_list_empty))

    val enumerationAreaErrorVisibility = object : ObservableField<Boolean>(itemCount) {
        override fun get() = itemCount.get() ?: 0 == 0
    }

    override fun quickstartData(latitude: Double, longitude: Double, radius: Double) {
        quickstartVisibility.set(false)
        viewMapVisibility.set(true)
        val quickJsonArea = GeoJsonEnumerationArea.createFromQuickstart(latitude, longitude, radius)
        adapter.data = mutableListOf(Pair(quickJsonArea, true))
        enumerationAreaErrorVisibility.set(false)
        itemCount.set(adapter.itemCount)
    }

    fun loadEnumerations(featureCollection: FeatureCollection?) {
        featureCollection?.let {
            adapter.data?.addAll(it.features.map { GeoJsonEnumerationArea.createFromFeature(it) }.map { Pair(it, false) })
            adapter.notifyDataSetChanged()
            itemCount.set(adapter.itemCount)
        }
    }

    override val progress: Int
        get() = 2
    override val backEnabled: ObservableField<Boolean> = ObservableField(true)
    override val nextEnabled: ObservableField<Boolean> = object : ObservableField<Boolean>(viewMapVisibility) {
        override fun get(): Boolean? {
            return viewMapVisibility.get() ?: false
        }
    }
    override fun onNextClicked(view: View) {
        adapter.data?.let {
            configBuildManager.addEnumerationAreas(it.map { it.first })
            val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.configFrame, MapboxConfigFragment())
                    .addToBackStack(MapboxConfigFragment::class.qualifiedName)
                    .commit()
        }
    }

    override fun onBackClicked(view: View) {
        val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
        fragmentManager.popBackStack()
    }

    override fun onDatasetChanged() = itemCount.set(adapter.itemCount)
}
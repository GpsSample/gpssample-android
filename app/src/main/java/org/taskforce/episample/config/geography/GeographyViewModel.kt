package org.taskforce.episample.config.geography

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import org.taskforce.episample.R
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.base.StepperCallback
import org.taskforce.episample.config.language.LanguageService

class GeographyViewModel(
        val languageService: LanguageService,
        private val stepper: Stepper,
        val adapter: EnumerationAreaAdapter) :
        ViewModel(), StepperCallback, QuickstartReceiver, OnDatasetChangedListener {

    init {
        languageService.update = {
            quickstartButtonText.set(languageService.getString(R.string.config_geography_quickstart))
            quickstartExplanation.set(languageService.getString(R.string.config_geography_quickstart_explanation))
            viewMapText.set(languageService.getString(R.string.config_geography_quickstart_alternate))
            enumerationAreaTitle.set(languageService.getString(R.string.config_enumeration_area_title))
            enumerationAreaError.set( languageService.getString(R.string.config_list_empty))
        }
        adapter.onDatasetChangedListener = this
    }
    
    lateinit var quickstart: () -> Unit

    val quickstartButtonText = ObservableField(languageService.getString(R.string.config_geography_quickstart))

    val quickstartExplanation = ObservableField(languageService.getString(R.string.config_geography_quickstart_explanation))

    val itemCount = ObservableField(0)

    val quickstartVisibility = object: ObservableField<Boolean>(itemCount) {
        override fun get(): Boolean? = itemCount.get()!! == 0
    }

    val viewMapVisibility = object: ObservableField<Boolean>(quickstartVisibility) {
        override fun get(): Boolean? = itemCount.get()!! > 0
    }

    val viewMapText = ObservableField(languageService.getString(R.string.config_geography_quickstart_alternate))

    val enumerationAreaTitle = object: ObservableField<String>(itemCount) {
        override fun get(): String? {
            return if (itemCount.get()!! > 0)
                "${languageService.getString(R.string.config_enumeration_area_title)} (${adapter.dataSize})"
            else
                languageService.getString(R.string.config_enumeration_area_title)
        }
    }

    val enumerationAreaError = ObservableField(languageService.getString(R.string.config_list_empty))

    val enumerationAreaErrorVisibility = ObservableField(true)

    override fun quickstartData(latitude: Double, longitude: Double, radius: Double) {
        quickstartVisibility.set(false)
        viewMapVisibility.set(true)
        adapter.data = EnumerationLayer("Quickstart Enumeration Layer").apply {
            enumerationAreas = mutableListOf(
                    EnumerationArea.quickstart(
                            "$latitude, $longitude ($radius km)",
                            latitude,
                            longitude,
                            radius))
        }
        enumerationAreaErrorVisibility.set(false)
        itemCount.set(adapter.itemCount)
        stepper.enableNext(true, GeographyFragment::class.java)
    }

    override fun onNext(): Boolean {
        return true
    }

    override fun onBack(): Boolean {
        return true
    }

    override fun enableNext() = viewMapVisibility.get() ?: false

    override fun enableBack() = true

    override fun onDatasetChanged() = itemCount.set(adapter.itemCount)
}
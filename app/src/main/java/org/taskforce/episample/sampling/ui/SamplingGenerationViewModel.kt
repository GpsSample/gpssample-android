package org.taskforce.episample.sampling.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.res.Resources
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.view.View
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.core.interfaces.DisplaySettings
import org.taskforce.episample.db.DateRange

class SamplingGenerationViewModel(val resources: Resources, val enumerationSubject: org.taskforce.episample.core.interfaces.EnumerationSubject, val collectManager: CollectManager, displaySettings: DisplaySettings) : ViewModel() {

    private val numberOfHouseHolds: LiveData<Int> = collectManager.getNumberOfValidEnumerations()
    private val householdCollectionDateRange: LiveData<DateRange> = collectManager.getValidEnumerationsDateRange()
    private var isGeneratingSample = ObservableBoolean(false)
    val loadingOverlayVisibility = object : ObservableField<Int>(isGeneratingSample) {
        override fun get(): Int {
            return if (isGeneratingSample.get()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
    val dateRangeText: LiveData<String> = Transformations.map(householdCollectionDateRange) {
        val fromDate: String = it.minimumDate?.let { minDate -> displaySettings.getFormattedDate(minDate, false) } ?: "N/A"
        val toDate: String = it.maximumDate?.let { maxDate -> displaySettings.getFormattedDate(maxDate, false) } ?: "N/A"
        resources.getString(R.string.recorded_from, fromDate, toDate)
    }

    val validHouseHoldsText: LiveData<String> = Transformations.map(numberOfHouseHolds) {
        val householdAmount = it
        val subject = if (householdAmount > 1 || householdAmount == 0) {
            enumerationSubject.plural
        } else {
            enumerationSubject.singular
        }

        resources.getString(R.string.valid, it.toString(), subject)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onGenerateClicked(view: View) {
        isGeneratingSample.set(true)
        collectManager.createSample()
    }
}

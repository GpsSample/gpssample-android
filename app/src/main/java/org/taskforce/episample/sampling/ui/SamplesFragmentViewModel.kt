package org.taskforce.episample.sampling.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.res.Resources
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.view.View
import org.taskforce.episample.R
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.core.interfaces.DisplaySettings
import org.taskforce.episample.core.interfaces.EnumerationSubject
import org.taskforce.episample.db.sampling.SampleEntity
import org.taskforce.episample.db.sampling.WarningEntity


class SamplesFragmentViewModel(val resources: Resources, val enumerationSubject: EnumerationSubject, val collectManager: CollectManager, val displaySettings: DisplaySettings) : ViewModel() {
    val warnings: LiveData<List<WarningEntity>> = collectManager.getWarnings()
    val sample: LiveData<SampleEntity> = collectManager.getSample()

    var areWarningsVisibile = ObservableBoolean(false)

    val warningsVisibility = object : ObservableInt(areWarningsVisibile) {
        override fun get(): Int {
            return if (areWarningsVisibile.get()) View.VISIBLE else View.GONE
        }
    }

    val seeWarningsVisibility = Transformations.map(warnings) {
        if (it.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    val sampleGeneratedOnExplanation = Transformations.map(LiveDataPair(warnings, sample)) {
        if (it.first.isNotEmpty()) {
            resources.getString(R.string.sample_generated_with_warnings, displaySettings.getFormattedDate(it.second.dateCreated, false))
        } else {
            resources.getString(R.string.sample_generate_on, displaySettings.getFormattedDate(it.second.dateCreated, false))
        }
    }

    val warningsText: LiveData<String> = Transformations.map(warnings) { warnings ->
        warnings.joinToString("\n") {
            "  •  ${it.warning}"
        }
    }

    val sampleTitle = Transformations.map(sample) {
        resources.getString(R.string.sample_generated, displaySettings.getFormattedDate(it.dateCreated, false))
    }

    val numberOfEnumerationsInSample: LiveData<Int> = collectManager.getNumberOfEnumerationsInSample()

    val numberOfEnumerationsText = Transformations.map(numberOfEnumerationsInSample) {
        if (it > 1 || it == 0) "$it ${enumerationSubject.plural}" else "$it ${enumerationSubject.singular}"
    }

    fun onSeeWarningsClicked(view: View) {
        areWarningsVisibile.set(!areWarningsVisibile.get())
    }

    fun onDeleteClicked(view: View) {
        collectManager.deleteSamples()
    }
}

class SamplesFragmentViewModelFactory(val resources: Resources, val enumerationSubject: EnumerationSubject, val collectManager: CollectManager, val displaySettings: DisplaySettings) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SamplesFragmentViewModel(resources, enumerationSubject, collectManager, displaySettings) as T
    }
}
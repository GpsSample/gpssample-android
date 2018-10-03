package org.taskforce.episample.config.sampling

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableField

class SamplingSelectionViewModelFactory(private val samplingType: SamplingMethodology,
                                        private val samplingInputType: SamplingUnits) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SamplingSelectionViewModel(ObservableField(samplingType),
                ObservableField(samplingInputType)) as T
    }
}
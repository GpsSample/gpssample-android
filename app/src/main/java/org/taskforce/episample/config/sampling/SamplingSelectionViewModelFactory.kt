package org.taskforce.episample.config.sampling

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableField
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.Stepper

class SamplingSelectionViewModelFactory(private val samplingType: SamplingSelectionType,
                                        private val samplingInputType: SamplingSelectionInputType,
                                        private val stepper: Stepper,
                                        private val configBuildManager: ConfigBuildManager,
                                        private val samplingSelectionDropdownProvider: SamplingDropdownProvider) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SamplingSelectionViewModel(ObservableField(samplingType),
                ObservableField(samplingInputType),
                stepper,
                configBuildManager,
                samplingSelectionDropdownProvider) as T
    }
}
package org.taskforce.episample.config.sampling

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class SamplingSelectionViewModelFactory(private val samplingMethod: SamplingMethod) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SamplingSelectionViewModel(samplingMethod) as T
    }
}
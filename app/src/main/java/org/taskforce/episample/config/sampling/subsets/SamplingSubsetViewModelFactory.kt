package org.taskforce.episample.config.sampling.subsets

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider


class SamplingSubsetViewModelFactory() : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SamplingSubsetViewModel() as T
    }
}
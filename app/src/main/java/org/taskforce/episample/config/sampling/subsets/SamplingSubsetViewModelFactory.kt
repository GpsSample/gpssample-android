package org.taskforce.episample.config.sampling.subsets

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider


class SamplingSubsetViewModelFactory(val isFixed: Boolean) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SamplingSubsetViewModel(isFixed) as T
    }
}
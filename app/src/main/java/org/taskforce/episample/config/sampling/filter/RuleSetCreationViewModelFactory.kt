package org.taskforce.episample.config.sampling.filter

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider


class RuleSetCreationViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RuleSetCreationViewModel() as T
    }
}
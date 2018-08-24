package org.taskforce.episample.main

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.language.LanguageService
import java.util.*

class MainViewModelFactory(private val application: Application,
                           private val languageService: LanguageService,
                           private val userName: String,
                           private val lastSynced: Date?,
                           private val isSupervisor: Boolean,
                           private val collectOnClick: () -> Unit,
                           private val navigateOnClick: () -> Unit,
                           private val syncOnClick: () -> Unit,
                           private val sampleOnClick: () -> Unit,
                           private val finalOnClick: () -> Unit): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(application,
                languageService,
                userName,
                lastSynced,
                isSupervisor,
                collectOnClick,
                navigateOnClick,
                syncOnClick,
                sampleOnClick,
                finalOnClick) as T
    }
}
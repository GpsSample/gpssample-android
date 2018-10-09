package org.taskforce.episample.collection.viewmodels

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Single
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.Enumeration
import org.taskforce.episample.core.interfaces.EnumerationSubject

class CollectAddViewModelFactory(private val application: Application,
                                 private val languageService: LanguageService,
                                 private val isLandmark: Boolean,
                                 private val saveButtonEnabledColor: Int,
                                 private val saveButtonDisabledColor: Int,
                                 private val saveButtonEnabledTextColor: Int,
                                 private val saveButtonDisabledTextColor: Int,
                                 private val goToNext: () -> Unit,
                                 private val takePicture: () -> Unit,
                                 private val showDuplicateGpsDialog: (enumeration: Enumeration?, subject: EnumerationSubject) -> Unit,
                                 private val showOutsideEnumerationAreaDialog: (latLng: LatLng, precision: Double) -> Unit) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CollectAddViewModel(application,
                languageService,
                isLandmark,
                saveButtonEnabledColor,
                saveButtonDisabledColor,
                saveButtonEnabledTextColor,
                saveButtonDisabledTextColor,
                goToNext,
                takePicture,
                showDuplicateGpsDialog,
                showOutsideEnumerationAreaDialog) as T
    }
}
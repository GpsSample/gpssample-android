package org.taskforce.episample.config.sampling

import android.arch.lifecycle.ViewModel
import android.databinding.Observable
import android.databinding.ObservableField
import android.widget.ArrayAdapter
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.base.StepperCallback
import org.taskforce.episample.toolbar.managers.LanguageManager

class SamplingSelectionViewModel(
        var selectionType: ObservableField<SamplingSelectionType>,
        var selectionInputType: ObservableField<SamplingSelectionInputType>,
        private val stepper: Stepper,
        private val configBuildManager: ConfigBuildManager,
        val samplingSelectionDropdownProvider: SamplingDropdownProvider) :
        ViewModel(), StepperCallback, SamplingSelectionOnDatasetChanged {

    var sampleSizeInput = ObservableField("")

    val isValid = object: ObservableField<Boolean>(sampleSizeInput, selectionType, selectionInputType) {
        override fun get(): Boolean? {
            return validateForm() == LanguageManager.undefinedStringResourceId
        }
    }

    private val validityObserver = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            stepper.enableNext(isValid.get()!!, SamplingSelectionFragment::class.java)
        }
    }

    init {
        isValid.addOnPropertyChangedCallback(validityObserver)
    }

    override fun onCleared() {
        isValid.removeOnPropertyChangedCallback(validityObserver)
        super.onCleared()
    }

    val samplingMethodTitle = ObservableField(R.string.config_sampling_method_select_title)

    val samplingMethodError = object: ObservableField<Int>(sampleSizeInput, isValid) {
        override fun get(): Int {
            return if (sampleSizeInput.get().isNullOrBlank()) {
                LanguageManager.undefinedStringResourceId
            } else {
                validateForm()
            }
        }
    }

    val errorEnabled = object: ObservableField<Boolean>(samplingMethodError) {
        override fun get() = samplingMethodError.get() != LanguageManager.undefinedStringResourceId
    }

    val samplingMethodHint = ObservableField(R.string.config_sampling_method_select_title)

    val sampleSizeHint = ObservableField(R.string.config_sampling_method_size)

    private val sampleSize: Double
        get() = sampleSizeInput.get()?.toDoubleOrNull() ?: 0.0

    fun validateForm(): Int {
        val newValue = sampleSizeInput.get()

        newValue?.toDoubleOrNull()?.let {
            return if (it < 0) {
                R.string.config_sampling_method_input_low_error
            } else if (selectionInputType.get()!! == SamplingSelectionInputType.PERCENT &&
                    it > 100
            ) {
                R.string.config_sampling_method_input_percent_high_error
            } else {
                LanguageManager.undefinedStringResourceId
            }
        } ?: run {
            return R.string.config_sampling_method_input_low_error
        }
    }

    override fun onNext() =
            if (isValid.get()!!) {
                configBuildManager.setSamplingMethod(SamplingMethod(selectionType.get()!!,
                        selectionInputType.get()!!,
                        sampleSize))
                true
            } else {
                false
            }

    override fun onBack() = true

    override fun enableNext() = isValid.get()!!

    override fun enableBack() = true

    override fun onSamplingSelectionTypeChanged(type: SamplingSelectionType) {
        selectionType.set(type)
    }

    override fun onSamplingSelectionInputChanged(input: SamplingSelectionInputType) {
        selectionInputType.set(input)
    }
}

interface SamplingDropdownProvider {
    var samplingSelectionTypeAdapter: ArrayAdapter<String>
    var samplingSelectionInputTypeAdapter: ArrayAdapter<String>
}
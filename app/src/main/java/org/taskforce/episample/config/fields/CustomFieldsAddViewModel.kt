package org.taskforce.episample.config.fields

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.widget.ArrayAdapter
import io.reactivex.Observable
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.db.config.customfield.CustomDateType
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.utils.bindDelegate

class CustomFieldsAddViewModel(
        languageService: LanguageService,
        val textColorInverseDisabled: Int,
        val textColorInverse: Int,
        val configBuildManager: ConfigBuildManager,
        val customFieldProvider: CustomFieldTypeProvider,
        val close: () -> Unit) :
        BaseObservable() {

    init {
        languageService.update = {
            title = languageService.getString(R.string.config_fields_add_title)
            save = languageService.getString(R.string.config_add_save)
            fieldNameHint = languageService.getString(R.string.config_fields_add_name_hint)
            customKeyHint = languageService.getString(R.string.config_fields_add_key_hint)
            fieldTypeText = languageService.getString(R.string.config_fields_add_dropdown_hint)
            containsPiiText = languageService.getString(R.string.config_fields_add_pii)
            requiredText = languageService.getString(R.string.config_fields_add_required)
            integerOnlyText = languageService.getString(R.string.config_fields_add_integer_only)
            dateExplanation = languageService.getString(R.string.config_fields_add_date_explanation)
            date = languageService.getString(R.string.config_fields_add_date_date)
            time = languageService.getString(R.string.config_fields_add_date_time)
            defaultTimeText = languageService.getString(R.string.config_fields_add_default_time_text)
        }
    }

    @get:Bindable
    var title by bindDelegate(languageService.getString(R.string.config_fields_add_title))

    @get:Bindable
    var save by bindDelegate(languageService.getString(R.string.config_add_save))

    @get:Bindable
    var fieldNameHint by bindDelegate(languageService.getString(R.string.config_fields_add_name_hint))

    @get:Bindable
    var customKeyHint by bindDelegate(languageService.getString(R.string.config_fields_add_key_hint))

    @get:Bindable
    var fieldTypeText by bindDelegate(languageService.getString(R.string.config_fields_add_dropdown_hint))

    @get:Bindable
    var containsPiiText by bindDelegate(languageService.getString(R.string.config_fields_add_pii))

    @get:Bindable
    var containsPii by bindDelegate(false)

    @get:Bindable
    var integerOnlyText by bindDelegate(languageService.getString(R.string.config_fields_add_integer_only))

    @get:Bindable
    var integerOnly by bindDelegate(false)

    @get:Bindable
    var requiredText by bindDelegate(languageService.getString(R.string.config_fields_add_required))

    @get:Bindable
    var required by bindDelegate(false)

    @get:Bindable
    var requiredVisibility by bindDelegate(true)

    @get:Bindable
    var dropdownVisibility by bindDelegate(false)

    @get:Bindable
    var integerOnlyVisibility by bindDelegate(false)

    @get:Bindable
    var dateOptionVisibility by bindDelegate(false)

    @get:Bindable
    var dropdownAdd by bindDelegate(languageService.getString(R.string.config_fields_add_another))

    @get:Bindable
    var dateExplanation by bindDelegate(languageService.getString(R.string.config_fields_add_date_explanation))

    @get:Bindable
    var date by bindDelegate(languageService.getString(R.string.config_fields_add_date_date))

    @get:Bindable
    var dateInput: Boolean by bindDelegate(false, { _, _ ->
        checkSaveAvailable()
    })

    @get:Bindable
    var time by bindDelegate(languageService.getString(R.string.config_fields_add_date_date_time))

    @get:Bindable
    var timeInput: Boolean by bindDelegate(false, { _, _ ->
        checkSaveAvailable()
    })

    @get:Bindable
    var defaultTimeText by bindDelegate(languageService.getString(R.string.config_fields_add_default_time_text))

    @get:Bindable
    var useDefaultTimeInput by bindDelegate(false)

    @get:Bindable
    var fieldName: String? by bindDelegate<String?>(null, { _, _ ->
        checkSaveAvailable()
    })

    @get:Bindable
    var customKey by bindDelegate<String?>(null)

    @get:Bindable
    var saveColor by bindDelegate(textColorInverseDisabled)

    @get:Bindable
    var saveEnabled by bindDelegate(false, { _, newValue ->
        saveColor = if (newValue) {
            textColorInverse
        } else {
            textColorInverseDisabled
        }
    })

    private val isDate: Boolean
        get() = dateInput || timeInput

    private val datePropertyInput: CustomDateType?
        get() {
            return if (dateInput && timeInput) {
                CustomDateType.DATE_TIME
            } else if (dateInput) {
                CustomDateType.DATE
            } else {
                CustomDateType.TIME
            }
            return null
        }

    private val properties: Map<String, Any>
        get() {
            val map = mutableMapOf(
                    CustomFieldTypeConstants.USE_CURRENT_TIME to useDefaultTimeInput,
                    CustomFieldTypeConstants.INTEGER_ONLY to integerOnly,
                    CustomFieldTypeConstants.DROPDOWN_ITEMS to dropdownAdapter.dropdownItems
            )
            datePropertyInput?.let {
                map[CustomFieldTypeConstants.DATE] = it
            }
            return map
        }

    private fun checkSaveAvailable() {
        saveEnabled = fieldName?.isNotBlank() == true && when (customFieldType) {
            CustomFieldType.TEXT -> {
                true
            }
            CustomFieldType.CHECKBOX -> {
                true
            }
            CustomFieldType.DROPDOWN -> {
                dropdownAdapter.dropdownItems.fold(true, { acc, next ->
                    acc && next.value?.isBlank() == false
                })
            }
            CustomFieldType.DATE -> {
                isDate
            }
            CustomFieldType.NUMBER -> {
                true
            }
        }
    }

    val dropdownAdapter = CustomFieldsAddDropdownAdapter()

    lateinit var customFieldType: CustomFieldType

    init {
        customFieldProvider.fieldTypeObservable.subscribe {
            when (it) {
                CustomFieldType.NUMBER -> {
                    integerOnlyVisibility = true
                    requiredVisibility = true
                    dateOptionVisibility = false
                    dropdownVisibility = false
                }
                CustomFieldType.TEXT -> {
                    integerOnlyVisibility = false
                    requiredVisibility = true
                    dateOptionVisibility = false
                    dropdownVisibility = false
                }
                CustomFieldType.DROPDOWN -> {
                    integerOnlyVisibility = false
                    requiredVisibility = true
                    dateOptionVisibility = false
                    dropdownVisibility = true
                }
                CustomFieldType.DATE -> {
                    integerOnlyVisibility = false
                    requiredVisibility = true
                    dateOptionVisibility = true
                    dropdownVisibility = false
                }
                CustomFieldType.CHECKBOX -> {
                    integerOnlyVisibility = false
                    requiredVisibility = false
                    dateOptionVisibility = false
                    dropdownVisibility = false
                }
            }
            customFieldType = it
            checkSaveAvailable()
        }
        dropdownAdapter.dropdownInputObservableObservable.subscribe {
            it.subscribe {
                checkSaveAvailable()
            }
        }
    }

    fun addDropdown() {
        dropdownAdapter.dropdownSize++
        dropdownAdapter.notifyItemInserted(dropdownAdapter.dropdownSize - 1)
    }

    fun save() {
        configBuildManager.addCustomField(
                CustomField(
                        false,
                        false,
                        true,
                        required,
                        containsPii,
                        fieldName!!,
                        customFieldType,
                        properties
                )
        )
        close()
    }
}

interface CustomFieldTypeProvider {
    var fieldTypeAdapter: ArrayAdapter<String>
    val fieldTypeObservable: Observable<CustomFieldType>
}
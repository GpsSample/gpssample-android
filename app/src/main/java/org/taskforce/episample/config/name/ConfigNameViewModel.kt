package org.taskforce.episample.config.name

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import android.support.v4.app.FragmentActivity
import android.view.View
import org.taskforce.episample.R
import org.taskforce.episample.config.base.BaseConfigViewModel
import org.taskforce.episample.config.base.Config
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.geography.GeographyFragment
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.toolbar.managers.LanguageManager



class ConfigNameViewModel(
        application: Application,
        private val configBuildManager: ConfigBuildManager)
    : AndroidViewModel(application), BaseConfigViewModel {

    val configRepository = ConfigRepository(getApplication())
    val takenNamesData = Transformations.map(configRepository.getAvailableConfigs(), {
        return@map it!!.map({ it.name }).toSet()
    })

    private var takenNames: Set<String> = setOf()

    val takenNamesObservable: Observer<Set<String>> = Observer {
        takenNames = it ?: setOf()
    }

    init {
//        stepper.enableNext(false, ConfigNameFragment::class.java)
        takenNamesData.observeForever(takenNamesObservable)
    }

    override fun onCleared() {
        super.onCleared()
        takenNamesData.removeObserver(takenNamesObservable)
        configRepository.cleanUp()
    }

    var backingName: String? = configBuildManager.config.name

    val name = object : ObservableField<String>() {
        override fun get(): String? = backingName

        override fun set(value: String?) {
            backingName = value
            error.set(validateName(value!!))
            notifyChange()
//            stepper.enableNext(!isInsufficientLength(get()!!), ConfigNameFragment::class.java)
        }
    }

    override val backEnabled = ObservableField(false)

    override val nextEnabled = object : ObservableField<Boolean>(name) {
        override fun get(): Boolean? {
            return isNameValid
        }
    }

    private val isNameValid: Boolean
        get() {
            return validateName(name.get()!!) == LanguageManager.undefinedStringResourceId
        }

    val hint = ObservableField(R.string.config_name_hint)
    val error = ObservableField(LanguageManager.undefinedStringResourceId)

    private fun isDuplicate(name: String) =
            takenNames.contains(name)

    private fun validateName(name: String): Int {
        if (isDuplicate(name)) {
            return R.string.config_name_duplicate_error
        }

        if (isInsufficientLength(name)) {
            return R.string.config_name_length_error_short
        }

        if (isLengthTooLong(name)) {
            return R.string.config_name_length_error_long
        }

        return LanguageManager.undefinedStringResourceId
    }

    private fun isInsufficientLength(name: String) =
            name.length < Config.nameMinChars || name.isBlank()

    private fun isLengthTooLong(name: String) = name.length > Config.nameMaxChars

    override val progress: Int
        get() = 1

    override fun onNextClicked(view: View) {
        try {
            configBuildManager.setName(name.get()!!)

            val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                    .replace(R.id.configFrame, GeographyFragment())
                    .addToBackStack(GeographyFragment::class.qualifiedName)
                    .commit()
        } catch (e: ClassCastException) {
            //TODO
        }
    }

    override fun onBackClicked(view: View) {
        //NOP
    }

}
package org.taskforce.episample.config.base

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.utils.bindDelegate
import org.taskforce.episample.utils.inverse

class ConfigViewModel(
        val configBuildManager: ConfigBuildManager,
        languageService: LanguageService,
        stepperButtonColor: Int,
        stepperButtonDisabledColor: Int,
        private val closeKeyboard: () -> Unit,
        private val success: () -> Unit,
        private val viewPager: ViewPager) : BaseObservable() {

    @get:Bindable
    var backText by bindDelegate(languageService.getString(R.string.config_back))

    @get:Bindable
    var nextText by bindDelegate(languageService.getString(R.string.config_next))

    @get:Bindable
    var completeness by bindDelegate(configBuildManager.config.completeness, { oldValue, newValue ->
        viewPager.currentItem = newValue - 1
        if (newValue == configScreenCount) {
            nextText = languageService.getString(R.string.config_done)
        } else {
            nextText = languageService.getString(R.string.config_next)
        }
    })

    @get:Bindable
    val configScreenCount
        get() = 8

    private val stepperCallbacks = mutableSetOf<Pair<Int, StepperCallback?>>()

    val stepperStateObservable
        get() = stepperStateSubject as Observable<Int>

    private val stepperStateSubject = BehaviorSubject.create<Int>()

    init {
        configBuildManager.configCompletenessObservable.subscribe {
            completeness = it
        }
        languageService.update = {
            backText = languageService.getString(R.string.config_back)
            nextText = if (completeness != configScreenCount) {
                languageService.getString(R.string.config_next)
            } else {
                languageService.getString(R.string.config_done)
            }
        }

    }
}

interface StepperCallback {
    fun onNext(): Boolean
    fun onBack(): Boolean
    fun enableNext(): Boolean
    fun enableBack(): Boolean
}

interface Stepper {
    fun enableNext(value: Boolean, screen: Class<out Fragment>)
    fun enableBack(value: Boolean, screen: Class<out Fragment>)
    fun next()
    fun back()
    fun addCallback(screen: Class<out Fragment>, callback: StepperCallback)
    fun removeCallback(callback: StepperCallback)
    val backEnabled: Boolean
    val nextEnabled: Boolean
}
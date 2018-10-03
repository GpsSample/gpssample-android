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
        private val viewPager: ViewPager) : BaseObservable(), Stepper {

    @get:Bindable
    var backText by bindDelegate(languageService.getString(R.string.config_back))

    @get:Bindable
    var nextText by bindDelegate(languageService.getString(R.string.config_next))

    @get:Bindable
    override var backEnabled by bindDelegate(false, { _, newValue ->
        backButtonTextColor = if (newValue) {
            stepperButtonColor
        } else {
            stepperButtonDisabledColor
        }
    })

    @get:Bindable
    var backButtonTextColor by bindDelegate(stepperButtonDisabledColor)

    @get:Bindable
    override var nextEnabled by bindDelegate(false, { _, newValue ->
        nextButtonTextColor = if (newValue) {
            stepperButtonColor
        } else {
            stepperButtonDisabledColor
        }
    })

    @get:Bindable
    var nextButtonTextColor by bindDelegate(stepperButtonDisabledColor)

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
        get() = ConfigAdapter.configFragmentMap.size

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

        nextEnabled = queryCallbacks(viewPager.currentItem, StepperCallback::enableNext)
    }

    override fun addCallback(screen: Class<out Fragment>, callback: StepperCallback) {
        ConfigAdapter.configFragmentMap.inverse[screen]?.let {
            stepperCallbacks.add(Pair(it, callback))
        }
    }

    override fun removeCallback(callback: StepperCallback) {
        stepperCallbacks.removeAll(stepperCallbacks.filter {
            it.second == callback
        })
    }

    override fun enableNext(value: Boolean, screen: Class<out Fragment>) {
        if (ConfigAdapter.configFragmentMap.inverse[screen] == completeness - 1) {
            nextEnabled = value
        }
    }

    override fun enableBack(value: Boolean, screen: Class<out Fragment>) {
        if (ConfigAdapter.configFragmentMap.inverse[screen] == completeness - 1) {
            backEnabled = value
        }
    }

    override fun next() {
        if (nextEnabled && queryCallbacks(viewPager.currentItem, StepperCallback::onNext)) {
            if (completeness != configScreenCount) {
                completeness++
                nextEnabled = queryCallbacks(viewPager.currentItem, StepperCallback::enableNext)
                backEnabled = true

                closeKeyboard()
                stepperStateSubject.onNext(completeness)
            } else {
                success()
            }
        }
    }

    override fun back() {
        if (backEnabled && queryCallbacks(viewPager.currentItem, StepperCallback::onBack)) {
            completeness--
            backEnabled = completeness != 1
            stepperStateSubject.onNext(completeness)
            nextEnabled = queryCallbacks(viewPager.currentItem, StepperCallback::enableNext)
        }
    }

    private fun queryCallbacks(page: Int, target: StepperCallback.() -> Boolean) =
            stepperCallbacks
                    .filter {
                        it.first == page
                    }
                    .map {
                        it.second?.let {
                            target(it)
                        } ?: false
                    }
                    .fold(true) { current, next ->
                        current && next
                    }

    internal fun removeCallbacks() = stepperCallbacks.clear()
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
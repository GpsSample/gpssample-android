package org.taskforce.episample.config.sampling.subsets

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.support.v4.app.FragmentActivity
import android.view.View
import org.greenrobot.eventbus.EventBus
import org.taskforce.episample.R
import org.taskforce.episample.config.base.BaseConfigViewModel
import org.taskforce.episample.config.sampling.SamplingUnits
import org.taskforce.episample.config.settings.display.DisplaySettingsFragment

class SamplingSubsetViewModel(isFixed: Boolean) : ViewModel(), BaseConfigViewModel {
    val eventBus = EventBus.getDefault()

    val isFixedChecked = object: ObservableBoolean(isFixed) {
        override fun set(value: Boolean) {
            super.set(value)
            val samplingUnit = when(value) {
                true -> SamplingUnits.FIXED
                false -> SamplingUnits.PERCENT
            }
            eventBus.post(Event.SamplingUnitsChanged(samplingUnit))
        }
    }
    val events = MutableLiveData<Event>()

    val fixedCheckedChanged = View.OnClickListener { _ ->
        if (!isFixedChecked.get()) {
            isFixedChecked.set(true)
        }
    }

    val percentageCheckedChanged = View.OnClickListener { _ ->
        if (isFixedChecked.get()) {
            isFixedChecked.set(false)
        }
    }

    val addRuleSetClicked = View.OnClickListener { view ->
        events.value = Event.AddRuleSet
    }

    override val progress: Int
        get() = 5
    override val backEnabled: ObservableField<Boolean> = ObservableField(true)
    override val nextEnabled: ObservableField<Boolean> = ObservableField(false)

    override fun onNextClicked(view: View) {
        val fragmentManager = (view.context as FragmentActivity).supportFragmentManager

        fragmentManager
                .beginTransaction()
                .replace(R.id.configFrame, DisplaySettingsFragment())
                .addToBackStack(DisplaySettingsFragment::class.qualifiedName)
                .commit()
    }

    override fun onBackClicked(view: View) {
        val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
        fragmentManager.popBackStack()
    }

    sealed class Event {
        object AddRuleSet : Event()
        class SamplingUnitsChanged(val samplingUnit: SamplingUnits): Event()
    }
}

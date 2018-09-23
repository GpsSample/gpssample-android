package org.taskforce.episample.config.sampling

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.view.View

class SamplingSubsetViewModel : ViewModel() {
    val isFixedChecked = ObservableBoolean(true)
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
        events.value = Event.AddRuleSet()
    }

    sealed class Event {
        class AddRuleSet : Event()
    }
}

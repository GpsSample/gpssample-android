package org.taskforce.episample.config.sampling.filter

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.view.View


class RuleSetCreationViewModel: ViewModel() {
    val closeEvents = MutableLiveData<Event>()
    val isAnyChecked = ObservableBoolean(true)
    val anyCheckedChanged = View.OnClickListener { _ ->
        if (!isAnyChecked.get()) {
            isAnyChecked.set(true)
        }
    }
    val allCheckedChanged = View.OnClickListener { _ ->
        if (isAnyChecked.get()) {
            isAnyChecked.set(false)
        }
    }

    val name = ObservableField<String>()

    fun closePressed(view: View) {
        closeEvents.value = Event.CloseEvent()
    }

    fun savePressed(view: View) {
        closeEvents.value = Event.SaveEvent()
    }

    fun addRulePressed(view: View) {
        closeEvents.value = Event.AddRuleEvent()
    }

    abstract class Event {
        class CloseEvent: Event()
        class SaveEvent: Event()
        class AddRuleEvent: Event()
    }
}
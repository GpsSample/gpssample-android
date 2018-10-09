package org.taskforce.episample.config.sampling.no_grouping

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.BindingAdapter
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.design.widget.TextInputLayout
import android.support.v4.app.FragmentActivity
import android.view.View
import org.greenrobot.eventbus.EventBus
import org.taskforce.episample.R
import org.taskforce.episample.config.base.BaseConfigViewModel
import org.taskforce.episample.config.sampling.SamplingUnits
import org.taskforce.episample.config.settings.display.DisplaySettingsFragment
import org.taskforce.episample.db.filter.RuleSet


class SamplingNoGroupViewModel(incomingSamplingUnit: SamplingUnits, val methodologyId: String) : ViewModel(), BaseConfigViewModel {
    val eventBus = EventBus.getDefault()
    var samplingUnit: SamplingUnits = incomingSamplingUnit
        set(value) {
            field = value
            amount.set("")
            error.notifyChange()
        }
    var amount: ObservableField<String> = ObservableField("")
    var error = object : ObservableInt(amount) {
        override fun get(): Int {
            return if (isValid()) {
                -1
            } else {
                when (samplingUnit) {
                    SamplingUnits.PERCENT -> R.string.percentage_amount_error
                    SamplingUnits.FIXED -> R.string.household_amount_error
                }
            }
        }
    }


    fun isValid(): Boolean {
        var isValid: Boolean? = null
        try {
            isValid = when (samplingUnit) {
                SamplingUnits.FIXED -> amount.get()?.let { isAmountValidHouseholdCount(it.toInt()) }
                SamplingUnits.PERCENT -> amount.get()?.let { isAmountValidPercentage(it.toDouble()) }
            }
        } catch (exception: Throwable) {
            //NOP
        }
        return isValid == true
    }

    private fun isAmountValidHouseholdCount(amount: Int): Boolean {
        return amount > 0
    }

    private fun isAmountValidPercentage(amount: Double): Boolean {
        return amount > 0.0 && amount < 100.0
    }

    override val progress: Int
        get() = 4
    override val backEnabled: ObservableField<Boolean> = ObservableField(true)
    override val nextEnabled: ObservableField<Boolean> = object : ObservableField<Boolean>(amount) {
        override fun get(): Boolean? {
            return isValid()
        }
    }

    override fun onNextClicked(view: View) {
        val ruleSet = RuleSet(methodologyId, "NO GROUPING", true, amount.get()!!.toInt())
        eventBus.post(RuleSetAdded(ruleSet))
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
}

class SamplingNoGroupViewModelProvider(private val samplingUnit: SamplingUnits, val methodologyId: String) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SamplingNoGroupViewModel(samplingUnit, methodologyId) as T
    }
}

@BindingAdapter("app:error")
fun setErrorText(view: TextInputLayout, observableInt: ObservableInt) {
    if (observableInt.get() != -1) {
        view.error = view.context.resources.getString(observableInt.get())
    } else {
        view.error = null
    }
}

class RuleSetAdded(val ruleSet: RuleSet)
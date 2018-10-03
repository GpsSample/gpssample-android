package org.taskforce.episample.config.sampling.no_grouping

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.BindingAdapter
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.design.widget.TextInputLayout
import android.support.v4.app.FragmentActivity
import android.view.View
import org.taskforce.episample.R
import org.taskforce.episample.config.base.BaseConfigViewModel
import org.taskforce.episample.config.settings.display.DisplaySettingsFragment


class SamplingNoGroupViewModel : ViewModel(), BaseConfigViewModel {
    var samplingUnit: SamplingUnit = SamplingUnit.HOUSEHOLDS
        set(value) {
            field = value
            amount.set("")
        }
    var amount: ObservableField<String> = ObservableField("")
    var error = object : ObservableInt(amount) {
        override fun get(): Int {
            return if (isValid()) {
                -1
            } else {
                when (samplingUnit) {
                    SamplingUnit.HOUSEHOLDS -> R.string.household_amount_error
                    SamplingUnit.PERCENT -> R.string.percentage_amount_error
                }
            }
        }
    }


    fun isValid(): Boolean {
        var isValid: Boolean? = null
        try {
            isValid = when (samplingUnit) {
                SamplingUnit.HOUSEHOLDS -> amount.get()?.let { isAmountValidHouseholdCount(it.toInt()) }
                SamplingUnit.PERCENT -> amount.get()?.let { isAmountValidPercentage(it.toDouble()) }
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
        //TODO probably have to store some data some where, huh. Here's where you should fire that off through event bus

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

class SamplingNoGroupViewModelProvider() : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SamplingNoGroupViewModel() as T
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
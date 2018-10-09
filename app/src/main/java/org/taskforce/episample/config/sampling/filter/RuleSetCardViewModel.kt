package org.taskforce.episample.config.sampling.filter

import android.databinding.BaseObservable
import org.greenrobot.eventbus.EventBus


class RuleSetCardViewModel(val ruleSetId: String, val name: String, val numberOfRules: Int, val isAny: Boolean, isUnitPercent: Boolean, samplingAmount: Int) : BaseObservable() {

    val eventBus = EventBus.getDefault()

    var samplingAmount: String = samplingAmount.toString()
        set(value) {
            if (value == "") {
                field = value
            } else if (isAmountValid(value.toInt())) {
                eventBus.post(UpdateSamplingAmount(ruleSetId, value.toInt()))
                field = value
            }
            eventBus.post(SamplingAmountChanged())
            notifyChange()
        }
    var isUnitPercent: Boolean = isUnitPercent
        set(value) {
            field = value
            samplingAmount = ""
            notifyChange()
        }
    val subtext: String
        get() = "${if (isAny) "Any" else "All"} of $numberOfRules Rules"
    val unitText: String
        get() = if (isUnitPercent) "%" else ""

    private fun isAmountValid(amount: Int) = when (isUnitPercent) {
        true -> amount in 1..100
        false -> amount > 0
    }

    fun isValid(): Boolean {
        try {
            return isAmountValid(samplingAmount.toInt())
        } catch (throwable: Throwable) {
            //NOP not valid
        }
        return false
    }

    class SamplingAmountChanged
    class UpdateSamplingAmount(val ruleSetId: String, val samplingAmount: Int)
}
package org.taskforce.episample.config.sampling.filter

import android.databinding.BaseObservable


class RuleSetCardViewModel(val ruleSetId: String, val name: String, val numberOfRules: Int, val isAny: Boolean): BaseObservable() {
    val subtext: String
        get() = "${if (isAny) "Any" else "All"} of $numberOfRules Rules"
}
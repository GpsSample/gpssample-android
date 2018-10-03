package org.taskforce.episample.config.sampling

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.support.v4.app.FragmentActivity
import android.view.View
import android.widget.AdapterView
import android.widget.RadioButton
import org.greenrobot.eventbus.EventBus
import org.taskforce.episample.R
import org.taskforce.episample.config.base.BaseConfigViewModel
import org.taskforce.episample.config.sampling.no_grouping.SamplingNoGroupFragment
import org.taskforce.episample.config.sampling.strata.SamplingStrataFragment
import org.taskforce.episample.config.sampling.subsets.SamplingSubsetFragment

class SamplingSelectionViewModel(
        val methodology: ObservableField<SamplingMethodology>,
        val units: ObservableField<SamplingUnits>) :
        ViewModel(), SamplingSelectionOnDatasetChanged, BaseConfigViewModel {
    val samplingGrouping = ObservableField<SamplingGrouping>(SamplingGrouping.SUBSETS)

    val eventBus: EventBus = EventBus.getDefault()

    val samplingMethodOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            // no-op
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            methodology.set(SamplingMethodology.values()[position])
            samplingMethodChanged()
        }
    }

    override fun samplingMethodologyChanged(type: SamplingMethodology) {
        methodology.set(type)
    }

    override fun samplingUnitsChanged(input: SamplingUnits) {
        units.set(input)
    }

    fun onSamplingGroupingRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            when (view.id) {
                R.id.subsetRadioButton -> {
                    if (view.isChecked) {
                        samplingGrouping.set(SamplingGrouping.SUBSETS)
                        eventBus.post(samplingGrouping.get())
                    }
                }
                R.id.strataRadioButton -> {
                    if (view.isChecked) {
                        samplingGrouping.set(SamplingGrouping.STRATA)
                        eventBus.post(samplingGrouping.get())
                    }
                }
                R.id.noneRadioButton -> {
                    if (view.isChecked) {
                        samplingGrouping.set(SamplingGrouping.NONE)
                        eventBus.post(samplingGrouping.get())
                    }
                }
                else -> {
                    //NOP
                }
            }
            samplingMethodChanged()
        }
    }

    fun samplingMethodChanged() {
        val newMethod = SamplingMethod(methodology.get()!!, units.get()!!, samplingGrouping.get()!!)
        eventBus.post(SamplingMethodChanged(newMethod))
    }

    override val progress: Int
        get() = 4
    override val backEnabled: ObservableField<Boolean> = ObservableField(true)
    override val nextEnabled: ObservableField<Boolean> = ObservableField(true)

    override fun onNextClicked(view: View) {
        val fragmentManager = (view.context as FragmentActivity).supportFragmentManager

        val fragment = when (samplingGrouping.get()) {
            SamplingGrouping.SUBSETS -> SamplingSubsetFragment()
            SamplingGrouping.STRATA -> SamplingStrataFragment()
            SamplingGrouping.NONE -> SamplingNoGroupFragment()
            null -> throw IllegalAccessError()
        }

        fragmentManager
                .beginTransaction()
                .replace(R.id.configFrame, fragment)
                .addToBackStack(fragment::class.qualifiedName)
                .commit()
    }

    override fun onBackClicked(view: View) {
        val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
        fragmentManager.popBackStack()
    }
}

class SamplingMethodChanged(val samplingMethod: SamplingMethod)
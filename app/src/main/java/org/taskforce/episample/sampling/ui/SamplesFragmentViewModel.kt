package org.taskforce.episample.sampling.ui

import android.app.AlertDialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.res.Resources
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.text.method.DigitsKeyListener
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import org.taskforce.episample.R
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.core.interfaces.DisplaySettings
import org.taskforce.episample.core.interfaces.EnumerationSubject
import org.taskforce.episample.db.sampling.SampleEntity
import org.taskforce.episample.db.sampling.WarningEntity


class SamplesFragmentViewModel(val resources: Resources, val enumerationSubject: EnumerationSubject, val collectManager: CollectManager, val displaySettings: DisplaySettings) : ViewModel() {
    private val warnings: LiveData<List<WarningEntity>> = collectManager.getWarnings()
    val sample: LiveData<SampleEntity?> = collectManager.getSample()
    val assignText: String = resources.getString(R.string.assign_households, enumerationSubject.plural)
    var areWarningsVisible = ObservableBoolean(false)

    private val numberOfNavigationPlans: LiveData<Int> = collectManager.getNumberOfNavigationPlans()

    val noNavigationPlansTextVisibility: LiveData<Int> = Transformations.map(numberOfNavigationPlans) {
        if (it > 0) View.GONE else View.VISIBLE
    }

    val warningsVisibility = object : ObservableInt(areWarningsVisible) {
        override fun get(): Int {
            return if (areWarningsVisible.get()) View.VISIBLE else View.GONE
        }
    }

    val seeWarningsVisibility: LiveData<Int> = Transformations.map(warnings) {
        if (it.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    val sampleGeneratedOnExplanation: LiveData<String> = Transformations.map(LiveDataPair(warnings, sample)) {
        if (it.first.isNotEmpty()) {
            resources.getString(R.string.sample_generated_with_warnings, it.second?.dateCreated?.let { date -> displaySettings.getFormattedDate(date, false) })
        } else {
            resources.getString(R.string.sample_generate_on, it.second?.dateCreated?.let { date -> displaySettings.getFormattedDate(date, false) })
        }
    }

    val warningsText: LiveData<String> = Transformations.map(warnings) { warnings ->
        warnings.joinToString("\n") {
            "  •  ${it.warning}"
        }
    }

    val sampleTitle: LiveData<String?> = Transformations.map(sample) {
        it?.let {
            resources.getString(R.string.sample_generated, displaySettings.getFormattedDate(it.dateCreated, false))
        }
    }

    private val numberOfEnumerationsInSample: LiveData<Int> = collectManager.getNumberOfEnumerationsInSample()

    val numberOfEnumerationsText: LiveData<String> = Transformations.map(numberOfEnumerationsInSample) {
        if (it > 1 || it == 0) "$it ${enumerationSubject.plural}" else "$it ${enumerationSubject.singular}"
    }

    fun onSeeWarningsClicked(view: View) {
        areWarningsVisible.set(!areWarningsVisible.get())
    }

    fun onAssignHouseholdsClicked(view: View) {
        val linearLayout = LinearLayout(view.context)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        linearLayout.layoutParams = params
        val marginInDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 18f, view.resources.displayMetrics).toInt()
        linearLayout.setPadding(marginInDp, 0, marginInDp, 0)

        val editText = EditText(view.context)
        editText.hint = view.resources.getString(R.string.num_of_enumerators)
        editText.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        editText.filters = arrayOf(DigitsKeyListener.getInstance("1234567890"))

        linearLayout.addView(editText)

        val dialogBuilder = AlertDialog.Builder(view.context)
        dialogBuilder
                .setTitle(R.string.create_sublists)
                .setMessage(R.string.number_of_enumerators)
                .setView(linearLayout)
                .setPositiveButton(R.string.okay) { dialog, which ->
                    val amount: Int? = try {
                        editText.text.toString().toInt()
                    } catch (throwable: Throwable) {
                        null
                    }
                    amount?.let {
                        val theSample = sample.value!!
                        val numberOfEnumerationsInSample = numberOfEnumerationsInSample.value!!
                        val numberOfNavigationPlansToMake: Int = if (it > numberOfEnumerationsInSample) numberOfEnumerationsInSample else it
                        if (numberOfNavigationPlansToMake > 0) {
                            collectManager.createNavigationPlans(theSample, numberOfNavigationPlansToMake)
                        } else {
                            Toast.makeText(view.context, resources.getString(R.string.cannot_create_navigation_plans, enumerationSubject.plural), Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .show()
    }

    fun onDeleteClicked(view: View) {
        val dialogBuilder = AlertDialog.Builder(view.context)
        dialogBuilder
                .setTitle(R.string.permanently_delete_sample)
                .setMessage(R.string.delete_sample_explanation)
                .setPositiveButton(R.string.delete_sample) { dialog, _ ->
                    collectManager.deleteSamples()
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()
    }
}

class SamplesFragmentViewModelFactory(val resources: Resources, val enumerationSubject: EnumerationSubject, val collectManager: CollectManager, val displaySettings: DisplaySettings) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SamplesFragmentViewModel(resources, enumerationSubject, collectManager, displaySettings) as T
    }
}
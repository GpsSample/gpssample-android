package org.taskforce.episample.core.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import org.taskforce.episample.R
import org.taskforce.episample.config.sampling.filter.RuleSetCreationActivity
import java.util.*

class DatePickerFragment : DialogFragment() {

    private lateinit var datePicker: DatePicker

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()

        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_date, null)
        datePicker = view.findViewById(R.id.spinnerDatePicker) as DatePicker
        datePicker.updateDate(c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        )

        builder.setView(view)
        builder
                .setPositiveButton(getString(R.string.submit)) { _, _ ->
                    val data = Intent()
                    data.putExtra(EXTRA_CUSTOM_FIELD_ID, arguments!!.getString(ARG_CUSTOM_FIELD_ID))
                    data.putExtra(EXTRA_YEAR, datePicker.year)
                    data.putExtra(EXTRA_MONTH, datePicker.month)
                    data.putExtra(EXTRA_DAY_OF_MONTH, datePicker.dayOfMonth)
                    data.putExtra(EXTRA_SHOW_TIME_PICKER_AFTER, arguments!!.getBoolean(ARG_SHOW_TIME_PICKER_AFTER))
                    if (targetFragment != null) {
                        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
                    } else if (activity is RuleSetCreationActivity) {
                        (activity as RuleSetCreationActivity).setDatePickerResult(data)
                    }
                    dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                    // Send the negative button event back to the host activity
                    if (targetFragment != null) {
                        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, null)
                    }
                    dismiss()
                }
        return builder.create()
    }

    companion object {
        const val EXTRA_YEAR = "EXTRA_YEAR"
        const val EXTRA_MONTH = "EXTRA_MONTH"
        const val EXTRA_DAY_OF_MONTH = "EXTRA_DAY_OF_MONTH"
        const val EXTRA_SHOW_TIME_PICKER_AFTER = "EXTRA_SHOW_TIME_PICKER_AFTER"
        const val EXTRA_CUSTOM_FIELD_ID = "EXTRA_CUSTOM_FIELD_ID"

        private const val ARG_SHOW_TIME_PICKER_AFTER = "ARG_SHOW_TIME_PICKER_AFTER"
        private const val ARG_CUSTOM_FIELD_ID = "ARG_CUSTOM_FIELD_ID"

        fun newInstance(customFieldId: String, showTimePickerAfter: Boolean): DatePickerFragment {
            val fragment = DatePickerFragment()
            fragment.arguments = Bundle()
            fragment.arguments?.putString(ARG_CUSTOM_FIELD_ID, customFieldId)
            fragment.arguments?.putBoolean(ARG_SHOW_TIME_PICKER_AFTER, showTimePickerAfter)
            return fragment
        }
    }
}
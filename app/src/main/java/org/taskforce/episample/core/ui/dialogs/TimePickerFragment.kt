package org.taskforce.episample.core.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.TimePicker
import org.taskforce.episample.R
import org.taskforce.episample.config.sampling.filter.RuleSetCreationActivity
import java.util.*

class TimePickerFragment : DialogFragment() {

    private lateinit var timePicker: TimePicker

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_time, null)
        timePicker = view.findViewById(R.id.spinnerTimePicker) as TimePicker

        builder.setView(view)
        builder.setPositiveButton(getString(R.string.submit)) { dialog, id ->
            val data = Intent()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                data.putExtra(EXTRA_HOUR, timePicker.hour)
                data.putExtra(EXTRA_MINUTE, timePicker.minute)
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                data.putExtra(EXTRA_HOUR, timePicker.currentHour)
                data.putExtra(EXTRA_MINUTE, timePicker.currentMinute)
            }

            data.putExtra(EXTRA_CUSTOM_FIELD_ID, arguments!!.getString(ARG_CUSTOM_FIELD_ID))
            data.putExtra(EXTRA_CHOSEN_DATE, arguments!!.getLong(ARG_CHOSEN_DATE, 0))

            if (targetFragment != null) {
                targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
            } else if (activity is RuleSetCreationActivity) {
                (activity as RuleSetCreationActivity).setTimePickerResult(data)
            }
            dismiss()
        }
                .setNegativeButton(getString(R.string.cancel)) { dialog, id ->
                    // Send the negative button event back to the host activity
                    dismiss()
                }
        return builder.create()
    }

    companion object {
        const val EXTRA_CUSTOM_FIELD_ID = "EXTRA_CUSTOM_FIELDf_ID"
        const val EXTRA_HOUR = "EXTRA_HOUR"
        const val EXTRA_MINUTE = "EXTRA_MINUTE"
        const val EXTRA_CHOSEN_DATE = "EXTRA_CHOSEN_DATE"

        private const val ARG_CUSTOM_FIELD_ID = "ARG_CUSTOM_FIELD_ID"
        private const val ARG_CHOSEN_DATE = "ARG_CHOSEN_DATE"

        fun newInstance(customFieldId: String, chosenDate: Date? = null): TimePickerFragment {
            val fragment = TimePickerFragment()
            fragment.arguments = Bundle()
            fragment.arguments?.putString(ARG_CUSTOM_FIELD_ID, customFieldId)
            chosenDate?.time?.let {
                fragment.arguments?.putLong(ARG_CHOSEN_DATE, it)
            }

            return fragment
        }
    }
}
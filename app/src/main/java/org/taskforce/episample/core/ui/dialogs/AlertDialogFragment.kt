package org.taskforce.episample.core.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.taskforce.episample.R

class AlertDialogFragment() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val titleId = arguments!!.getInt(ARG_TITLE_RES_ID)
        val messageId = arguments!!.getInt(ARG_MESSAGE_RES_ID)

        return AlertDialog.Builder(requireActivity())
                .setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(R.string.okay, { _, _ ->
                    dismiss()
                })
                .create()
    }

    companion object {
        const val ARG_TITLE_RES_ID = "ARG_TITLE_RES_ID"
        const val ARG_MESSAGE_RES_ID = "ARG_MESSAGE_RES_ID"

        fun newInstance(titleResId: Int, messageResId: Int): DialogFragment {
            val fragment = AlertDialogFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_TITLE_RES_ID, titleResId)
                putInt(ARG_MESSAGE_RES_ID, messageResId)
            }
            return fragment
        }
    }
}
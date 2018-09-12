package org.taskforce.episample.core.ui.dialogs

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.taskforce.episample.R
import org.taskforce.episample.databinding.FragmentTextInputDialogBinding
import org.taskforce.episample.toolbar.managers.LanguageManager

class TextInputDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentTextInputDialogBinding>(inflater, R.layout.fragment_text_input_dialog, container, false)

        val titleResId = arguments!!.getInt(ARG_TITLE_RES_ID, LanguageManager.undefinedStringResourceId)
        val hintResId = arguments!!.getInt(ARG_HINT_RES_ID, LanguageManager.undefinedStringResourceId)
        binding.vm = TextInputDialogViewModel(requireActivity().application,
                titleResId,
                hintResId,
                { textInput -> submit(textInput) },
                { goBack() }
        )

        binding.setLifecycleOwner(this)

        return binding.root
    }


    fun submit(textInput: String) {
        val data = Intent()
        data.putExtra(EXTRA_TEXT_INPUT_ID, textInput)
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
        dismiss()
    }

    fun goBack() {
        dismiss()
    }

    companion object {
        const val TAG = "TextInputDialogFragment"
        const val EXTRA_TEXT_INPUT_ID = "EXTRA_TEXT_INPUT_ID"

        private const val ARG_TITLE_RES_ID = "ARG_TITLE_RES_ID"
        private const val ARG_HINT_RES_ID = "ARG_HINT_RES_ID"



        fun newInstance(titleResId: Int, hintResId: Int): TextInputDialogFragment {
            val fragment = TextInputDialogFragment()
            fragment.arguments = Bundle()
            fragment.arguments!!.putInt(ARG_TITLE_RES_ID, titleResId)
            fragment.arguments!!.putInt(ARG_HINT_RES_ID, hintResId)
            return fragment
        }
    }
}
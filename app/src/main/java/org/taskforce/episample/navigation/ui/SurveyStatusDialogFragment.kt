package org.taskforce.episample.navigation.ui

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import kotlinx.android.synthetic.main.fragment_config_fields.*
import kotlinx.android.synthetic.main.fragment_survey_status_dialog.*
import org.taskforce.episample.R
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.core.ui.dialogs.TextInputDialogFragment
import org.taskforce.episample.databinding.FragmentSurveyStatusDialogBinding

class SurveyStatusDialogFragment: DialogFragment() {

    lateinit var surveyStatusViewModel: SurveyStatusViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        surveyStatusViewModel = ViewModelProviders.of(this).get(SurveyStatusViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        val binding = DataBindingUtil.inflate<FragmentSurveyStatusDialogBinding>(inflater, R.layout.fragment_survey_status_dialog, container, false)

        binding.vm = surveyStatusViewModel
        binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        skipImageView.setOnClickListener({
            val fragment = TextInputDialogFragment.newInstance(R.string.dialog_skip_reason_title, R.string.dialog_skip_reason_hint)
            fragment.setTargetFragment(this, GET_SKIP_REASON_CODE)
            fragment.show(requireFragmentManager(), TextInputDialogFragment.TAG)
        })

        problemImageView.setOnClickListener({
            val fragment = TextInputDialogFragment.newInstance(R.string.dialog_skip_reason_title, R.string.dialog_skip_reason_hint)
            fragment.setTargetFragment(this, GET_PROBLEM_REASON_CODE)
            fragment.show(requireFragmentManager(), TextInputDialogFragment.TAG)
        })

        submit.setOnClickListener({
            submit()
        })
        goBack.setOnClickListener({
            goBack()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        val textInput = data?.getStringExtra(TextInputDialogFragment.EXTRA_TEXT_INPUT)

        when (requestCode) {
            GET_SKIP_REASON_CODE -> {
                textInput?.let {
                    surveyStatusViewModel.surveyStatus.postValue(SurveyStatus.Skipped(it))
                }
            }
            GET_PROBLEM_REASON_CODE -> {
                textInput?.let {
                    surveyStatusViewModel.surveyStatus.postValue(SurveyStatus.Problem(it))
                }
            }
        }
    }

    fun submit() {
        surveyStatusViewModel.surveyStatus.value?.let {
            val navigationItemId = arguments!!.getString(ARG_NAVIGATION_ITEM_ID)
            surveyStatusViewModel.navigationManager.updateSurveyStatus(navigationItemId, it)
            dismiss()
        }
    }

    fun goBack() {
        dismiss()
    }

    companion object {
        const val TAG = "SurveyStatusDialogFragment"
        private const val ARG_NAVIGATION_ITEM_ID = "ARG_NAVIGATION_ITEM_ID"
        private const val ARG_SURVEY_STATUS = "ARG_SURVEY_STATUS"
        const val EXTRA_SURVEY_STATUS = "EXTRA_SURVEY_STATUS"

        const val GET_SKIP_REASON_CODE = 1
        const val GET_PROBLEM_REASON_CODE = 2

        fun newInstance(navigationItemId: String, surveyStatus: SurveyStatus): SurveyStatusDialogFragment {
            val fragment = SurveyStatusDialogFragment()
            val arguments = Bundle()
            arguments.putString(ARG_NAVIGATION_ITEM_ID, navigationItemId)
            arguments.putParcelable(ARG_SURVEY_STATUS, surveyStatus)
            fragment.arguments = arguments
            return fragment
        }
    }
}
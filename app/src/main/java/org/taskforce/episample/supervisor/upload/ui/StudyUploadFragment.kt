package org.taskforce.episample.supervisor.upload.ui

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.drive.Drive
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentStudyUploadBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import javax.inject.Inject

class StudyUploadFragment : Fragment() {
    
    @Inject
    lateinit var languageManager: LanguageManager
    lateinit var languageService: LanguageService

    lateinit var viewModel: StudyUploadViewModel
    lateinit var toolbarViewModel: ToolbarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)
        
        languageService = LanguageService(languageManager)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        toolbarViewModel = ToolbarViewModel(
                languageService,
                languageManager,
                HELP_TARGET) {
                    requireActivity().supportFragmentManager.popBackStack()
                }
        toolbarViewModel.title = languageService.getString(R.string.final_report)

        val binding = DataBindingUtil.inflate<FragmentStudyUploadBinding>(inflater, R.layout.fragment_study_upload, container, false)
        viewModel = ViewModelProviders.of(this,
                StudyUploadViewModelFactory(requireActivity().application,
                        performSignIn))
                .get(StudyUploadViewModel::class.java)
        binding.vm = viewModel
        binding.toolbarVm = toolbarViewModel
        binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SIGN_IN_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(requireContext(), R.string.final_upload_sign_in_failed, Toast.LENGTH_SHORT).show()
                return
            } else {
                viewModel.continueUpload()
            }
        }
    }

    private val performSignIn: () -> Unit = {
        val signInClient = buildGoogleSignInClient(requireContext().applicationContext)

        val silentSignInTask = signInClient.silentSignIn()
        silentSignInTask.addOnFailureListener {
            startActivityForResult(signInClient.signInIntent, SIGN_IN_CODE)
        }
        silentSignInTask.addOnSuccessListener {
            viewModel.continueUpload()
        }
    }

    private fun buildGoogleSignInClient(context: Context): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .build()
        return GoogleSignIn.getClient(context, signInOptions)
    }

    companion object {
        const val SIGN_IN_CODE = 5
        const val HELP_TARGET = "#studyUpload"

        fun newInstance(): Fragment {
            return StudyUploadFragment()
        }
    }
}

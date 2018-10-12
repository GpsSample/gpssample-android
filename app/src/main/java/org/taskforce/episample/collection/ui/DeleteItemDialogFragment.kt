package org.taskforce.episample.collection.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.viewmodels.DeleteItemDialogViewModel
import org.taskforce.episample.collection.viewmodels.DeleteItemDialogViewModelFactory
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentDeleteItemDialogBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

@SuppressLint("ValidFragment")
class DeleteItemDialogFragment(val delete: () -> Unit): DialogFragment() {
    
    @Inject
    lateinit var languageManager: LanguageManager
    
    lateinit var subject: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        (requireActivity().application as EpiApplication).component.inject(this)
        
        subject = arguments!!.getString(DELETE_SUBJECT_KEY)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentDeleteItemDialogBinding>(inflater, R.layout.fragment_delete_item_dialog, container, false)
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        binding.vm = ViewModelProviders.of(this, DeleteItemDialogViewModelFactory(LanguageService(languageManager),
                {
                    delete()
                },
                {
                    this@DeleteItemDialogFragment.dismiss()
                },
                subject)).get(DeleteItemDialogViewModel::class.java)
        
        
        return binding.root
    }

    companion object {
        const val DELETE_SUBJECT_KEY = "DeleteSubjectKey"
        
        fun newInstance(delete: () -> Unit, subject: String): DialogFragment {
            val fragment = DeleteItemDialogFragment(delete)
            fragment.arguments = Bundle().apply { 
                putString(DELETE_SUBJECT_KEY, subject)
            }
            return fragment
        }
    }
}
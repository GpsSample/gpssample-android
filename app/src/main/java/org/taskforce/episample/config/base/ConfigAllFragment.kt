package org.taskforce.episample.config.base

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_config_all.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.study.StudyCreateFragment
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.databinding.FragmentConfigAllBinding
import org.taskforce.episample.db.config.Study
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import javax.inject.Inject

class ConfigAllFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var transferManager: TransferManager

    lateinit var viewModel: ConfigAllViewModel
    lateinit var languageService: LanguageService

    private var study: Study? = null

    val back = {
        requireActivity().supportFragmentManager.popBackStack()
    }

    private val showError: (String) -> Unit = {
        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT)
                .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)
        languageService = LanguageService(languageManager)
        viewModel = ViewModelProviders.of(this@ConfigAllFragment, ConfigAllViewModelFactory(
                requireActivity().application,
                languageService,
                transferManager,
                {
                    if (study == null) {
                        requireActivity().supportFragmentManager
                                .beginTransaction()
                                .addToBackStack(StudyCreateFragment::class.java.name)
                                .replace(R.id.configFrame, StudyCreateFragment.newInstance(it.id)).commit()
                    }  else {
                        // TODO get design advice on UX
                        Toast.makeText(requireContext(), "Cannot create more than one study", Toast.LENGTH_SHORT).show()
                    }
                },
                {
                    // TODO open config summary
                    Toast.makeText(requireContext(), "TODO", Toast.LENGTH_SHORT).show()
                },
                showError
        )).get(ConfigAllViewModel::class.java)
        viewModel.back = back
        viewModel.study.observe(this, Observer {
            study = it
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentConfigAllBinding>(inflater,
                R.layout.fragment_config_all, container, false)

        binding.languageService = languageService
        binding.vm = viewModel
        binding.setLifecycleOwner(this)

        binding.toolbarVm = ToolbarViewModel(
                LanguageService(languageManager),
                languageManager,
                HELP_TARGET,
                back
        ).apply {
            languageManager.languageEventObservable.subscribe {
                title = languageManager.getString(R.string.config_all_toolbar_title)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ConfigAllAdapter(viewModel.menuObservable, languageService.getString(R.string.study_date), {
            // TODO open config summary
            Toast.makeText(requireContext(), "TODO", Toast.LENGTH_SHORT).show()
        })

        fragment_config_all_recyclerView.adapter = adapter
        fragment_config_all_recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        viewModel.availableConfigs.observe(this, Observer { configs ->
            adapter.setConfigs(configs ?: listOf())
        })
    }

    companion object {
        private const val HELP_TARGET = "#configAll"
    }
}
package org.taskforce.episample.config.fields

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_config_fields_add.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.config.base.ConfigManager
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentConfigFieldsAddBinding
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.getCompatColor
import javax.inject.Inject

class CustomFieldsAddFragment : Fragment(), CustomFieldTypeProvider {

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var languageManager: LanguageManager

    override val fieldTypeObservable: Observable<CustomFieldType>
        get() = fieldTypeSubject as Observable<CustomFieldType>

    override lateinit var fieldTypeAdapter: ArrayAdapter<String>

    private val fieldTypeSubject = BehaviorSubject.create<CustomFieldType>()

    lateinit var configBuildViewModel: ConfigBuildViewModel

    private val customFieldsSource = CustomFieldType.values()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        (requireActivity().application as EpiApplication).component.inject(this)

        fieldTypeAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            addAll(customFieldsSource.map {
                it.name.toLowerCase().capitalize()
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigFieldsAddBinding.inflate(inflater).apply {
                vm = CustomFieldsAddViewModel(
                        LanguageService(languageManager),
                        requireContext().getCompatColor(R.color.textColorInverseDisabled),
                        requireContext().getCompatColor(R.color.textColorInverse),
                        configBuildViewModel.configBuildManager,
                        this@CustomFieldsAddFragment,
                        {
                            requireActivity().supportFragmentManager?.popBackStack()
                        })
            }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fieldType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fieldTypeSubject.onNext(customFieldsSource[position])
            }

        }
    }

    companion object {
        fun newInstance(): Fragment = CustomFieldsAddFragment()
    }
}
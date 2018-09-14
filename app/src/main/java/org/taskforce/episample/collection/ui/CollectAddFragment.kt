package org.taskforce.episample.collection.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolylineOptions
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_collect_add.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.managers.CollectionItemMarkerManager
import org.taskforce.episample.collection.managers.generateView
import org.taskforce.episample.collection.managers.generateViewModel
import org.taskforce.episample.collection.viewmodels.CollectAddViewModel
import org.taskforce.episample.collection.viewmodels.CollectAddViewModelFactory
import org.taskforce.episample.collection.viewmodels.CustomDropdownViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.CustomField
import org.taskforce.episample.core.ui.dialogs.DatePickerFragment
import org.taskforce.episample.core.ui.dialogs.TimePickerFragment
import org.taskforce.episample.databinding.FragmentCollectAddBinding
import org.taskforce.episample.db.config.customfield.CustomDateType
import org.taskforce.episample.db.config.customfield.metadata.DateMetadata
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import org.taskforce.episample.utils.getCompatColor
import java.util.*
import javax.inject.Inject

class CollectAddFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var locationManager: LocationManager
    lateinit var collectIconFactory: CollectIconFactory
    lateinit var markerManager: CollectionItemMarkerManager

    lateinit var collectViewModel: CollectAddViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        collectIconFactory = CollectIconFactory(requireContext().resources)
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val mapFragment = SupportMapFragment()

        val binding = FragmentCollectAddBinding.inflate(inflater).apply {
            childFragmentManager
                    .beginTransaction()
                    .replace(R.id.collectAddMap, mapFragment)
                    .commit()

            mapFragment.getMapAsync {
                markerManager = CollectionItemMarkerManager(collectIconFactory, it)

                it.isMyLocationEnabled = true
                it.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }

            collectViewModel = ViewModelProviders.of(this@CollectAddFragment, CollectAddViewModelFactory(
                    requireActivity().application,
                    LanguageService(languageManager),
                    Single.create<GoogleMap> { single ->
                        mapFragment.getMapAsync {
                            single.onSuccess(it)
                        }
                    },
                    Observable.create<Location> { emitter ->
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, object : LocationListener {
                            override fun onLocationChanged(location: Location) {
                                emitter.onNext(location)
                            }

                            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                                //NOP
                            }

                            override fun onProviderEnabled(provider: String?) {
                                //NOP
                            }

                            override fun onProviderDisabled(provider: String?) {
                                //NOP
                            }
                        })
                    },
                    arguments?.getBoolean(IS_LANDMARK) == true,
                    requireContext().getCompatColor(R.color.colorAccent),
                    requireContext().getCompatColor(R.color.textColorDisabled),
                    requireContext().getCompatColor(R.color.textColorInverse),
                    requireContext().getCompatColor(R.color.textColorDetails),
                    {
                        requireFragmentManager()
                                .popBackStack()
                    }, {
                // TODO: Launch intent to take picture
                Toast.makeText(requireContext(), "TODO", Toast.LENGTH_SHORT).show()
            }
            )).get(CollectAddViewModel::class.java)

            landmarkImageSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val landmarkType = collectViewModel.config.landmarkTypes[position]
                    collectViewModel.selectedLandmark.postValue(landmarkType)
                }
            }

            vm = collectViewModel

            toolbarVm = ToolbarViewModel(
                    LanguageService(languageManager),
                    languageManager,
                    HELP_TARGET, {
                requireActivity().supportFragmentManager.popBackStack()
            })

            collectViewModel.customFields.forEach {
                if (!it.isAutomatic) {
                    it.generateViewModel(collectViewModel.config.displaySettings,
                            {
                                showDatePicker(it)
                            },
                            requireContext()).apply {
                        val view = it.generateView(root, requireContext(), this, customFieldHolder)
                        if (this is CustomDropdownViewModel) {
                            this.view = view
                        }
                        collectViewModel.addCustomFieldViewModel(this)
                        customFieldHolder.addView(
                                view,
                                LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ))
                    }
                }
            }

            val userSettings = collectViewModel.config.userSettings
            val vm = CollectGpsPrecisionViewModel(userSettings.gpsMinimumPrecision,
                    userSettings.gpsPreferredPrecision,
                    requireContext().getCompatColor(R.color.colorError),
                    requireContext().getCompatColor(R.color.colorWarning),
                    requireContext().getCompatColor(R.color.gpsAcceptable))

            gpsVm = vm

            collectViewModel.gpsVm = vm
        }
        binding.setLifecycleOwner(this)

        collectViewModel.gpsBreadcrumbs.observe(this@CollectAddFragment, Observer { breadcrumbs ->
            mapFragment.getMapAsync { map ->
                val breadCrumbPath = PolylineOptions().clickable(false).color(R.color.greyHighlights)
                breadcrumbs?.sortedBy { it.dateCreated }?.forEach { breadCrumbPath.add(it.location) }
                map.addPolyline(breadCrumbPath)
            }
        })

        collectViewModel.collectItems.observe(this, Observer { items ->
            mapFragment.getMapAsync {
                markerManager.addMarkerDiff(items ?: emptyList())
            }
        })

        collectViewModel.landmarkTypes.observe(this@CollectAddFragment, Observer {
            val types = it ?: emptyList()

            val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapter.addAll(types.map { it.name })

            this@CollectAddFragment.landmarkImageSelector.adapter = adapter
        })

        val enumerationSubject = collectViewModel.config.enumerationSubject
        binding.toolbarVm?.title = if (arguments?.getBoolean(IS_LANDMARK) == true) {
            languageManager.getString(R.string.collect_add_landmark_title)
        } else {
            languageManager.getString(R.string.collect_add_item_title, enumerationSubject.singular.capitalize())
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }

        when (requestCode) {
            GET_DATE_CODE -> {
                val customFieldId = data.getStringExtra(DatePickerFragment.EXTRA_CUSTOM_FIELD_ID)
                val year = data.getIntExtra(DatePickerFragment.EXTRA_YEAR, 0)
                val month = data.getIntExtra(DatePickerFragment.EXTRA_MONTH, 1) + 1
                val day = data.getIntExtra(DatePickerFragment.EXTRA_DAY_OF_MONTH, 0)
                val showTimeNext = data.getBooleanExtra(DatePickerFragment.EXTRA_SHOW_TIME_PICKER_AFTER, false)

                val cal = Calendar.getInstance()
                cal.set(year, month, day)

                if (showTimeNext) {
                    showTimePickerDialog(customFieldId, cal.time)
                } else {
                    collectViewModel.updateDateField(customFieldId, cal.time)
                }
            }
            GET_TIME_CODE -> {
                val customFieldId = data.getStringExtra(TimePickerFragment.EXTRA_CUSTOM_FIELD_ID)
                val chosenDateLong = data.getLongExtra(TimePickerFragment.EXTRA_CHOSEN_DATE, 0)
                val hour = data.getIntExtra(TimePickerFragment.EXTRA_HOUR, 0)
                val minute = data.getIntExtra(TimePickerFragment.EXTRA_MINUTE, 0)
                val date = Date(chosenDateLong)

                val cal = Calendar.getInstance()
                cal.time = date
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                collectViewModel.updateDateField(customFieldId, cal.time)
            }
        }
    }

    private fun showDatePicker(customField: CustomField) {
        (customField.metadata as? DateMetadata)?.let { dateMetadata ->
            when (dateMetadata.dateType) {
                CustomDateType.DATE -> showDatePickerDialog(customField, false)
                CustomDateType.TIME -> showTimePickerDialog(customField.id)
                CustomDateType.DATE_TIME -> showDatePickerDialog(customField, true)
            }
        }
    }

    private fun showDatePickerDialog(customField: CustomField, showTimePickerAfter: Boolean = false) {
        val newFragment = DatePickerFragment.newInstance(customField.id, showTimePickerAfter)
        newFragment.setTargetFragment(this, GET_DATE_CODE)
        newFragment.show(fragmentManager, "datePicker")
    }

    private fun showTimePickerDialog(customFieldId: String, chosenDate: Date? = null) {
        val newFragment = TimePickerFragment.newInstance(customFieldId, chosenDate)
        newFragment.setTargetFragment(this, GET_TIME_CODE)
        newFragment.show(fragmentManager, "timePicker")
    }

    companion object {
        const val IS_LANDMARK = "isLandmark"
        const val HELP_TARGET = "#collectAdd"
        const val GET_TIME_CODE = 1
        const val GET_DATE_CODE = 2
    }
}
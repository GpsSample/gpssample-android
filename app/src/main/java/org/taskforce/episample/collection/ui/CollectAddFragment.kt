package org.taskforce.episample.collection.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolylineOptions
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_collect_add.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.managers.CollectionItemMarkerManager
import org.taskforce.episample.collection.managers.generateView
import org.taskforce.episample.collection.managers.generateViewModel
import org.taskforce.episample.collection.viewmodels.CollectAddViewModel
import org.taskforce.episample.collection.viewmodels.CustomDropdownViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentCollectAddBinding
import org.taskforce.episample.fileImport.models.LandmarkType
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import org.taskforce.episample.utils.getCompatColor
import javax.inject.Inject

class CollectAddFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var locationClient: FusedLocationProviderClient
    lateinit var collectIconFactory: CollectIconFactory
    lateinit var markerManager: CollectionItemMarkerManager

    lateinit var collectViewModel: CollectAddViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)

        locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
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

            collectViewModel = CollectAddViewModel(
                    requireActivity().application,
                    LanguageService(languageManager),
                    Single.create<GoogleMap> { single ->
                        mapFragment.getMapAsync {
                            single.onSuccess(it)
                        }
                    },
                    Observable.create<Location> { emitter ->
                        locationClient.requestLocationUpdates(LocationRequest.create().apply {
                            interval = 5000
                            fastestInterval = 1000
                            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        }, object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult?) {
                                result?.lastLocation?.let {
                                    emitter.onNext(it)
                                }
                            }
                        }, null)
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
            )

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

                    it.generateViewModel(requireContext()).apply {
                        val view = it.generateView(requireContext(), this, customFieldHolder)
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

    companion object {
        const val IS_LANDMARK = "isLandmark"
        const val HELP_TARGET = "#collectAdd"
    }
}
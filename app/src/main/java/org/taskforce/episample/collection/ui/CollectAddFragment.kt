package org.taskforce.episample.collection.ui

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.auth.AuthManager
import org.taskforce.episample.collection.viewmodels.CollectAddViewModel
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

    @Inject
    lateinit var authManager: AuthManager

    lateinit var locationClient: FusedLocationProviderClient

    val landmarkSelectSubject: BehaviorSubject<LandmarkType> = BehaviorSubject.create<LandmarkType>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)

        locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentCollectAddBinding.inflate(inflater).apply {

                val mapFragment = SupportMapFragment()
                childFragmentManager
                        .beginTransaction()
                        .replace(R.id.map, mapFragment)
                        .commit()

                val collectViewModel = CollectAddViewModel(
                        LanguageService(languageManager),
                        landmarkSelectSubject as Observable<LandmarkType>,
                        Observable.create<GoogleMap> { emitter ->
                            mapFragment.getMapAsync {
                                emitter.onNext(it)
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
                        }.apply {
                            // TODO observe live data collection of breadcrumbs
//                            gpsBreadcrumbManager.observeBreadcrumbs(CollectAddFragment::class.java.name,
//                                    map {
//                                        GpsBreadcrumb(authManager.username
//                                                ?: "Unknown", LatLng(it.latitude, it.longitude))
//                                    })
                        },
                        arguments?.getBoolean(IS_LANDMARK) == true,
                        requireContext().getCompatColor(R.color.colorAccent),
                        requireContext().getCompatColor(R.color.textColorDisabled),
                        requireContext().getCompatColor(R.color.textColorInverse),
                        requireContext().getCompatColor(R.color.textColorDetails),
                        ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            // TODO observe landmark types
                            add("TEMP_LANDMARK")
//                            addAll(studyManager.currentStudy!!.config.landmarkTypes.map {
//                                it.name
//                            })
                        },
// TODO add inject config gps settings
////                            studyManager.currentStudy!!.config.userSettings!!.gpsMinimumPrecision,
////                            studyManager.currentStudy!!.config.userSettings!!.gpsPreferredPrecision,
                            40.0, 15.0,
                        requireContext().getCompatColor(R.color.colorError),
                        requireContext().getCompatColor(R.color.colorWarning),
                        requireContext().getCompatColor(R.color.gpsAcceptable),
                        {
                            requireFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.mainFrame, CollectFragment())
                                    .addToBackStack(CollectAddFragment::class.java.name)
                                    .commit()
                        }
                )

                landmarkImageSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        // TODO get landmark list from db (through VM)
                        // studyManager.currentStudy!!.config.landmarkTypes[position]
                        landmarkSelectSubject.onNext(LandmarkType("ANY"))
                    }
                }

                // TODO custom field logic
//                studyManager.currentStudy?.config?.customFields?.forEach {
//                    if (!it.isAutomatic) {
//
//
//                        it.generateViewModel(requireContext()).apply {
//                            val view = it.generateView(requireContext(), this, customFieldHolder)
//                            if (this is CustomDropdownViewModel) {
//                                this.view = view
//                            }
//                            collectViewModel.addCustomFieldViewModel(this)
//                            customFieldHolder.addView(
//                                    view,
//                                    LinearLayout.LayoutParams(
//                                            LinearLayout.LayoutParams.MATCH_PARENT,
//                                            LinearLayout.LayoutParams.WRAP_CONTENT
//                                    ))
//                        }
//
//
//
//                    }
//                }

                vm = collectViewModel

                toolbarVm = ToolbarViewModel(
                        LanguageService(languageManager),
                        languageManager,
                        HELP_TARGET, {
                    requireActivity().supportFragmentManager.popBackStack()
                }).apply {
                    title = if (arguments?.getBoolean(IS_LANDMARK) == true) {
                        languageManager.getString(R.string.collect_add_landmark_title)
                    } else {
                        //TODO inject config enumeration subject
                        languageManager.getString(R.string.collect_add_item_title, "HOUSEHOLD")
                    }
                }

                gpsVm = CollectGpsPrecisionViewModel(
                        //                            // TODO add inject config gps settings
////                            studyManager.currentStudy!!.config.userSettings!!.gpsMinimumPrecision,
////                            studyManager.currentStudy!!.config.userSettings!!.gpsPreferredPrecision,
                            40.0, 15.0,
                        requireContext().getCompatColor(R.color.colorError),
                        requireContext().getCompatColor(R.color.colorWarning),
                        requireContext().getCompatColor(R.color.gpsAcceptable)
                )
            }.root

    companion object {
        const val IS_LANDMARK = "isLandmark"
        const val HELP_TARGET = "#collectAdd"
    }
}
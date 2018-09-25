package org.taskforce.episample.navigation.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import io.reactivex.Single
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.managers.CollectionItemMarkerManager
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.core.interfaces.CollectItem
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.core.ui.dialogs.TextInputDialogFragment
import org.taskforce.episample.databinding.FragmentNavigationBinding
import org.taskforce.episample.utils.getCompatColor


class NavigationFragment : Fragment(), GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var collectIconFactory: CollectIconFactory
    private lateinit var markerManager: CollectionItemMarkerManager

    private lateinit var navigationViewModel: NavigationViewModel
    private lateinit var navigationToolbarViewModel: NavigationToolbarViewModel
    private lateinit var navigationCardViewModel: NavigationCardViewModel

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var navigationPlanId: String

    private var googleMap: GoogleMap? = null
    private var lastKnownLocation: LatLng? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)
        navigationPlanId = arguments!!.getString(ARG_NAVIGATION_PLAN_ID)

        locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        collectIconFactory = CollectIconFactory(requireContext().resources)

        mapFragment = SupportMapFragment()

        Single.create<GoogleMap> { single ->
            mapFragment.getMapAsync {
                single.onSuccess(it)
                it.setOnMarkerClickListener(this@NavigationFragment)
                it.setOnMapClickListener(this@NavigationFragment)
            }
        }.subscribe({
            googleMap = it
            lastKnownLocation?.let { location ->
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(location, CollectViewModel.zoomLevel))
            }

        },
                {
                    //TODO: Display unable to load Google Map.
                }
        )

        navigationViewModel = ViewModelProviders.of(this@NavigationFragment,
                NavigationViewModelFactory(requireActivity().application,
                        navigationPlanId))
                .get(NavigationViewModel::class.java)
        lifecycle.addObserver(navigationViewModel.locationService)
        navigationViewModel.locationService.locationLiveData.observe(this, Observer {
            it?.let { pair ->
                val location = pair.first

                if (lastKnownLocation == null) {
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, CollectViewModel.zoomLevel))
                }
                lastKnownLocation = location
            }
        })
        navigationCardViewModel = ViewModelProviders.of(this@NavigationFragment,
                LiveNavigationCardViewModelFactory(
                        requireActivity().application,
                        navigationViewModel.config.userSettings,
                        navigationViewModel.locationService.locationLiveData,
                        requireContext().getCompatColor(R.color.colorError),
                        requireContext().getCompatColor(R.color.colorWarning),
                        requireContext().getCompatColor(R.color.gpsAcceptable),
                        { launchSurvey() },
                        { showSkipDialog() }
                )).get(LiveNavigationCardViewModel::class.java)

        navigationToolbarViewModel = ViewModelProviders.of(this@NavigationFragment,
                NavigationToolbarViewModelFactory(requireActivity().application,
                        R.string.navigation_plan)).get(NavigationToolbarViewModel::class.java)

        navigationViewModel.nextNavigationItem.observe(this, Observer { nextItem ->
            nextItem?.let {
                navigationCardViewModel.itemData.postValue(it)
                navigationCardViewModel.visibility.postValue(it != null)
            } ?: run {
                if (fragmentManager?.backStackEntryCount ?: 0 > 0) {
                    fragmentManager?.popBackStack()
                } else {
                    requireActivity().finish()
                }
            }

        })

        mapFragment.getMapAsync {
            markerManager = CollectionItemMarkerManager(collectIconFactory, it)

            it.isMyLocationEnabled = true
            it.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentNavigationBinding>(inflater, R.layout.fragment_navigation, container, false)
        setHasOptionsMenu(true)
        val toolbar = binding.root.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        childFragmentManager
                .beginTransaction()
                .replace(R.id.collectionMap, mapFragment)
                .commit()

        binding.vm = navigationViewModel
        binding.cardVm = navigationCardViewModel
        binding.toolbarVm = navigationToolbarViewModel
        binding.setLifecycleOwner(this)

        navigationViewModel.landmarks.observe(this, Observer { landmarks ->
            this@NavigationFragment.mapFragment.getMapAsync {
                markerManager.addMarkerDiff(landmarks ?: emptyList())
            }
        })

        navigationViewModel.navigationItems.observe(this, Observer { items ->
            this@NavigationFragment.mapFragment.getMapAsync {
                markerManager.addMarkerDiff(items ?: emptyList())
            }
        })
//
//        navigationViewModel.possiblePath.observe(this, Observer { breadcrumbs ->
//            this@NavigationFragment.mapFragment.getMapAsync { map ->
//                val breadCrumbPath = PolylineOptions()
//                        .pattern(listOf(Dot(), Gap(10.0F)))
//                        .jointType(JointType.ROUND)
//                        .width(15.0F)
//                        .clickable(false)
//                breadcrumbs?.sortedBy { it.dateCreated }?.forEach { breadCrumbPath.add(it.location) }
//                map.addPolyline(breadCrumbPath)
//            }
//        })
//
//        navigationViewModel.breadcrumbs.observe(this, Observer { breadcrumbs ->
//            this@NavigationFragment.mapFragment.getMapAsync { map ->
//                val breadCrumbPath = PolylineOptions()
//                        .width(5.0F)
//                        .clickable(false)
//                breadcrumbs?.sortedBy { it.dateCreated }?.forEach { breadCrumbPath.add(it.location) }
//                map.addPolyline(breadCrumbPath)
//            }
//        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.navigation, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.let {
            val collectItem = it.tag as CollectItem
            navigationCardViewModel.visibility.postValue(true)
            navigationCardViewModel.itemData.postValue(collectItem)
        }

        return true
    }

    override fun onMapClick(p0: LatLng?) {
        navigationCardViewModel.visibility.postValue(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        toolbar?.setNavigationOnClickListener {
            if (fragmentManager?.backStackEntryCount ?: 0 > 0) {
                fragmentManager?.popBackStack()
            } else {
                requireActivity().finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_help -> {
            }
            R.id.action_language -> {
                navigationToolbarViewModel.languageSelectVisibility.postValue(true)
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            GET_SKIP_REASON_CODE -> {
                val skipReason = data?.getStringExtra(TextInputDialogFragment.EXTRA_TEXT_INPUT)
                skipReason?.let { skipReason ->
                    navigationViewModel.nextNavigationItem.value?.id?.let { nextItem ->
                        navigationViewModel.navigationManager.updateSurveyStatus(nextItem, SurveyStatus.Skipped(skipReason), {
                            // no - op: change is observed elsewhere in UI
                        })
                    }
                }
            }
            PICK_FORM_REASON_CODE -> {
                val formUri = data?.data
                formUri?.let {
                    val intent = Intent(Intent.ACTION_EDIT)
                    intent.data = it
                    startActivity(intent)
                    navigationViewModel.launchedSurvey = true
                }
            }
            GET_SURVEY_STATUS_REASON_CODE -> {
                val surveyStatus = data?.getParcelableExtra<SurveyStatus>(SurveyStatusDialogFragment.EXTRA_SURVEY_STATUS)
                surveyStatus?.let { surveyStatus ->
                    navigationViewModel.nextNavigationItem?.value?.id?.let {
                        navigationViewModel.navigationManager.updateSurveyStatus(it, surveyStatus, {
                            // no - op: change is observed elsewhere in UI
                        })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (navigationViewModel.launchedSurvey) {
            navigationViewModel.nextNavigationItem.value?.let { navItem ->
                navItem.id?.let { id ->
                    val fragment = SurveyStatusDialogFragment.newInstance(id, navItem.surveyStatus)
                    fragment.setTargetFragment(this, GET_SURVEY_STATUS_REASON_CODE)
                    fragment.show(requireFragmentManager(), SurveyStatusDialogFragment.TAG)
                }
            }
        }

        navigationViewModel.launchedSurvey = false
    }

    private fun launchSurvey() {
        try {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "vnd.android.cursor.dir/vnd.odk.form"
            startActivityForResult(intent, PICK_FORM_REASON_CODE)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.odk_collect_not_installed, Toast.LENGTH_LONG)
                    .show()
        }
    }

    private fun showSkipDialog() {
        val fragment = TextInputDialogFragment.newInstance(R.string.dialog_skip_reason_title, R.string.dialog_skip_reason_hint)
        fragment.setTargetFragment(this, GET_SKIP_REASON_CODE)
        fragment.show(requireFragmentManager(), TextInputDialogFragment.TAG)
    }

    companion object {
        const val ARG_NAVIGATION_PLAN_ID = "ARG_NAVIGATION_PLAN_ID"

        const val GET_SKIP_REASON_CODE = 1
        const val PICK_FORM_REASON_CODE = 2
        const val GET_SURVEY_STATUS_REASON_CODE = 3

        fun newInstance(navigationPlanId: String): Fragment {
            val fragment = NavigationFragment()
            val arguments = Bundle()
            arguments.putString(ARG_NAVIGATION_PLAN_ID, navigationPlanId)
            fragment.arguments = arguments
            return fragment
        }
    }
}
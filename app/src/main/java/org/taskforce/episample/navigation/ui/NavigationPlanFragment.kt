package org.taskforce.episample.navigation.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions
import io.reactivex.Single
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.managers.CollectionItemMarkerManager
import org.taskforce.episample.collection.ui.CollectAddFragment
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.core.interfaces.CollectItem
import org.taskforce.episample.databinding.FragmentNavigationPlanBinding
import org.taskforce.episample.utils.getCompatColor

class NavigationPlanFragment : Fragment(), GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var collectIconFactory: CollectIconFactory
    private lateinit var markerManager: CollectionItemMarkerManager

    private lateinit var navigationPlanViewModel: NavigationPlanViewModel
    private lateinit var navigationToolbarViewModel: NavigationToolbarViewModel
    private lateinit var navigationCardViewModel: NavigationCardViewModel

    var adapter: NavigationItemAdapter? = null
    private lateinit var mapFragment: SupportMapFragment

    private var googleMap: GoogleMap? = null
    private var lastKnownLocation: LatLng? = null

    private lateinit var navigationPlanId: String

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)

        locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        collectIconFactory = CollectIconFactory(requireContext().resources)

        mapFragment = SupportMapFragment()
        navigationPlanId = arguments!!.getString(ARG_NAVIGATION_PLAN_ID)
        Single.create<GoogleMap> { single ->
            mapFragment.getMapAsync {
                single.onSuccess(it)
                it.setOnMarkerClickListener(this@NavigationPlanFragment)
                it.setOnMapClickListener(this@NavigationPlanFragment)
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

        navigationPlanViewModel = ViewModelProviders.of(this@NavigationPlanFragment,
                NavigationPlanViewModelFactory(
                        requireActivity().application,
                        navigationPlanId,
                        {
                            showNavigationScreen()
                        },
                        {
                            showAddLocationScreen()
                        })).get(NavigationPlanViewModel::class.java)
        lifecycle.addObserver(navigationPlanViewModel.locationService)
        navigationPlanViewModel.locationService.locationLiveData.observe(this, Observer {
            it?.let { pair ->
                val location = pair.first

                if (lastKnownLocation == null) {
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, CollectViewModel.zoomLevel))
                }
                lastKnownLocation = location
            }
        })
        navigationCardViewModel = ViewModelProviders.of(this@NavigationPlanFragment,
                NavigationPlanCardViewModelFactory(
                        requireActivity().application,
                        navigationPlanViewModel.config.userSettings,
                        navigationPlanViewModel.locationService.locationLiveData,
                        requireContext().getCompatColor(R.color.colorError),
                        requireContext().getCompatColor(R.color.colorWarning),
                        requireContext().getCompatColor(R.color.gpsAcceptable)

                )).get(NavigationPlanCardViewModel::class.java)

        navigationToolbarViewModel = ViewModelProviders.of(this@NavigationPlanFragment,
                NavigationToolbarViewModelFactory(requireActivity().application,
                        R.string.navigation_plan)).get(NavigationToolbarViewModel::class.java)

        mapFragment.getMapAsync {
            markerManager = CollectionItemMarkerManager(collectIconFactory, it)

            it.isMyLocationEnabled = true
            it.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentNavigationPlanBinding>(inflater, R.layout.fragment_navigation_plan, container, false)
        setHasOptionsMenu(true)
        val toolbar = binding.root.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        childFragmentManager
                .beginTransaction()
                .replace(R.id.collectionMap, mapFragment)
                .commit()

        binding.vm = navigationPlanViewModel
        binding.cardVm = navigationCardViewModel
        binding.toolbarVm = navigationToolbarViewModel
        binding.setLifecycleOwner(this)

        adapter = NavigationItemAdapter(CollectIconFactory(requireContext().resources),
                navigationPlanViewModel.config.displaySettings)
        binding.collectList.adapter = adapter

        navigationPlanViewModel.navigationItems.observe(this, Observer { items ->
            val sortedItems = items?.sortedBy { it.navigationOrder } ?: emptyList()
            adapter?.data = sortedItems
        })
        navigationPlanViewModel.collectItems.observe(this, Observer { items ->
            this@NavigationPlanFragment.mapFragment.getMapAsync {
                markerManager.addMarkerDiff(items ?: emptyList())
            }
        })

        navigationPlanViewModel.breadcrumbs.observe(this, Observer { breadcrumbs ->
            this@NavigationPlanFragment.mapFragment.getMapAsync { map ->
                val breadCrumbPath = PolylineOptions()
                        .width(5.0F)
                        .clickable(false)
                breadcrumbs?.sortedBy { it.dateCreated }?.forEach { breadCrumbPath.add(it.location) }
                map.addPolyline(breadCrumbPath)
            }
        })

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

    private fun showNavigationScreen() {
        requireFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, NavigationFragment.newInstance(navigationPlanId))
                .addToBackStack(NavigationFragment::class.java.name)
                .commit()
    }

    private fun showAddLocationScreen() {
        requireFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, CollectAddFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(CollectAddFragment.IS_LANDMARK, true)
                    }
                })
                .addToBackStack(CollectAddFragment::class.java.name)
                .commit()
    }

    companion object {
        private const val ARG_NAVIGATION_PLAN_ID = "ARG_NAVIGATION_PLAN_ID"

        fun newInstance(navigationPlanId: String): Fragment {
            val fragment = NavigationPlanFragment()
            val bundle = Bundle()
            bundle.putString(ARG_NAVIGATION_PLAN_ID, navigationPlanId)
            fragment.arguments = bundle
            return fragment
        }
    }
}
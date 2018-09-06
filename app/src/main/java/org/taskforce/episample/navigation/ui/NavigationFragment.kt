package org.taskforce.episample.navigation.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Single
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.managers.CollectionItemMarkerManager
import org.taskforce.episample.collection.ui.CollectAddFragment
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.databinding.FragmentNavigationBinding

class NavigationFragment : Fragment() {

    lateinit var locationClient: FusedLocationProviderClient
    lateinit var collectIconFactory: CollectIconFactory
    lateinit var markerManager: CollectionItemMarkerManager

    lateinit var navigationViewModel: NavigationViewModel
    lateinit var navigationToolbarViewModel: NavigationToolbarViewModel

    var adapter: NavigationItemAdapter? = null
    lateinit var mapFragment: SupportMapFragment

    private var googleMap: GoogleMap? = null
    private var lastKnownLocation: LatLng? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)

        locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        collectIconFactory = CollectIconFactory(requireContext().resources)

        mapFragment = SupportMapFragment()

        Single.create<GoogleMap> { single ->
            mapFragment.getMapAsync {
                single.onSuccess(it)
            }
        }.subscribe( {
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
                NavigationViewModelFactory(
                        requireActivity().application,
                        {
                            showAddLocationScreen()
                        })).get(NavigationViewModel::class.java)
        lifecycle.addObserver(navigationViewModel.locationService)
        navigationViewModel.locationService.locationLiveData.observe(this, Observer {
            it?.let { pair ->
                val location = pair.first
                val accuracy = pair.second

                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, CollectViewModel.zoomLevel))
                lastKnownLocation = location
            }
        })
        navigationToolbarViewModel = ViewModelProviders.of(this@NavigationFragment,
                NavigationToolbarViewModelFactory(requireActivity().application,
                        R.string.navigation_plan,
                        "HELP_TARGET",
                        {

                        })).get(NavigationToolbarViewModel::class.java)

        mapFragment.getMapAsync {
            markerManager = CollectionItemMarkerManager(collectIconFactory, it)

            it.isMyLocationEnabled = true
            it.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentNavigationBinding>(inflater, R.layout.fragment_navigation, container, false)

        childFragmentManager
                .beginTransaction()
                .replace(R.id.collectionMap, mapFragment)
                .commit()

        binding.vm = navigationViewModel

        adapter = NavigationItemAdapter(CollectIconFactory(requireContext().resources),
                navigationViewModel.config.displaySettings)
        binding.collectList.adapter = adapter

        navigationViewModel.enumerations.observe(this, Observer { items ->
            val sortedItems = items?.sortedByDescending { it.dateCreated } ?: emptyList()
            adapter?.data = sortedItems
        })
        navigationViewModel.collectItems.observe(this, Observer { items ->
            this@NavigationFragment.mapFragment.getMapAsync {
                markerManager.addMarkerDiff(items ?: emptyList())
            }
        })

        binding.toolbarVm = navigationToolbarViewModel

        binding.setLifecycleOwner(this)

        return binding.root
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
        fun newInstance(): Fragment {
            return NavigationFragment()
        }
    }
}
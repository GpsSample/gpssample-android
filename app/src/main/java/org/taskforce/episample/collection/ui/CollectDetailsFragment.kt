package org.taskforce.episample.collection.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import kotlinx.android.synthetic.main.fragment_collect_details.*
import org.taskforce.episample.BuildConfig
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.managers.MapboxItemMarkerManager
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.CollectItem
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.Enumeration
import org.taskforce.episample.core.interfaces.Landmark
import org.taskforce.episample.databinding.FragmentCollectDetailsBinding
import org.taskforce.episample.navigation.ui.NavigationToolbarViewModel
import org.taskforce.episample.navigation.ui.NavigationToolbarViewModelFactory
import org.taskforce.episample.utils.getCompatColor
import org.taskforce.episample.utils.toMapboxLatLng
import javax.inject.Inject

@SuppressLint("ValidFragment")
class CollectDetailsFragment(private val collectItem: CollectItem) : Fragment(), MapboxMap.OnMarkerClickListener {

    @Inject
    lateinit var config: Config

    lateinit var viewModel: CollectDetailsViewModel
    lateinit var toolbarViewModel: NavigationToolbarViewModel
    lateinit var gpsViewModel: CollectGpsPrecisionViewModel

    lateinit var detailItemAdapter: CollectDetailItemAdapter

    lateinit var collectIconFactory: CollectIconFactory
    private val markerManagerLiveData = MutableLiveData<MapboxItemMarkerManager>()

    private val mapPreferences: SharedPreferences
        get() = requireActivity().getSharedPreferences(MAP_PREFERENCE_NAMESPACE, Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(requireContext(), BuildConfig.MAPBOX_ACCESS_TOKEN)

        (requireActivity().application as EpiApplication).collectComponent?.inject(this)

        viewModel = ViewModelProviders.of(this).get(CollectDetailsViewModel::class.java)
        viewModel.data.postValue(collectItem)

        val userSettings = viewModel.config.userSettings
        val vm = CollectGpsPrecisionViewModel(userSettings.gpsMinimumPrecision,
                userSettings.gpsPreferredPrecision,
                requireContext().getCompatColor(R.color.colorError),
                requireContext().getCompatColor(R.color.colorWarning),
                requireContext().getCompatColor(R.color.gpsAcceptable))
        gpsViewModel = vm

        collectIconFactory = CollectIconFactory(requireContext().resources)

        detailItemAdapter = CollectDetailItemAdapter(collectIconFactory)
        detailItemAdapter.resources = requireContext().resources

        val title = when (collectItem) {
            is Enumeration -> config.enumerationSubject.singular
            is Landmark -> getString(R.string.landmark)
            else -> ""
        }

        toolbarViewModel = ViewModelProviders.of(this@CollectDetailsFragment,
                NavigationToolbarViewModelFactory(requireActivity().application,
                        R.string.view_item_title,
                        title)).get(NavigationToolbarViewModel::class.java)


    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val mapFragment = SupportMapFragment()

        val binding = DataBindingUtil.inflate<FragmentCollectDetailsBinding>(inflater,
                R.layout.fragment_collect_details,
                container,
                false).apply {
            childFragmentManager.beginTransaction()
                    .replace(R.id.collectDetailsMap, mapFragment)
                    .commit()

            mapFragment.getMapAsync {
                markerManagerLiveData.postValue(MapboxItemMarkerManager(requireContext(), mapPreferences, it))

                it.setOnMarkerClickListener(this@CollectDetailsFragment)

                it.moveCamera(CameraUpdateFactory.newLatLngZoom(collectItem.location.toMapboxLatLng(), CollectViewModel.zoomLevel))
            }

            collectDetailFields.layoutManager = LinearLayoutManager(requireContext())
            collectDetailFields.adapter = detailItemAdapter
        }
        binding.vm = viewModel
        binding.gpsVm = gpsViewModel
        binding.toolbarVm = toolbarViewModel
        binding.setLifecycleOwner(this)
        lifecycle.addObserver(viewModel.locationService)

        viewModel.customFields.observe(this, Observer {
            it?.let {
                detailItemAdapter.resources = resources
                detailItemAdapter.data = it.toMutableList()
                detailItemAdapter.notifyDataSetChanged()
            }
        })

        viewModel.gpsBreadcrumbs.observe(this, Observer { breadcrumbs ->
            mapFragment.getMapAsync { map ->
                val polylineOptions = mutableListOf<PolylineOptions>()
                breadcrumbs?.sortedBy { it.dateCreated }?.forEach {
                    if (it.startOfSession) {
                        polylineOptions.add(PolylineOptions()
                                .color(R.color.greyHighlights))
                    }

                    polylineOptions.last().add(it.location.toMapboxLatLng())
                }

                polylineOptions.forEach { breadCrumbPath ->
                    map.addPolyline(breadCrumbPath)
                }
            }
        })

        LiveDataPair(markerManagerLiveData, viewModel.collectItems).observe(this, Observer {
            it?.let { (markerManager, items) ->
                markerManager.addMarkerDiff(items)
            }
        })

        gpsViewModel.precision.set(collectItem.gpsPrecision)

        return binding.root
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar?.setNavigationOnClickListener {
            goBack()
        }
    }
    
    private fun goBack() {
        if (fragmentManager?.backStackEntryCount ?: 0 > 0) {
            fragmentManager?.popBackStack()
        } else {
            requireActivity().finish()
        }
        
        collectDetailsImage.setOnClickListener {
            collectItem.image?.let { imageUri ->
                val photoFragment = ViewPhotoFragment.newInstance(imageUri)
                photoFragment.show(requireFragmentManager(), ViewPhotoFragment::class.java.simpleName)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        markerManagerLiveData.observe(this, Observer {
            it?.let {
                it.applyLayerSettings()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.collect_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_help -> {
            }
            R.id.action_edit -> {
                val subject = when (collectItem) {
                    is Enumeration -> config.enumerationSubject.singular
                    is Landmark -> getString(R.string.landmark)
                    else -> ""
                }.toLowerCase()
                
                val deleteDialog = DeleteItemDialogFragment.newInstance(confirmedDelete, subject)
                deleteDialog.show(childFragmentManager, DeleteItemDialogFragment::class.java.simpleName)
            }
//            R.id.action_language -> {
//                toolbarViewModel.languageSelectVisibility.postValue(true)
//            }
        }
        return true
    }
    
    private val confirmedDelete: () -> Unit = {
        viewModel.deleteCollectItem()
        goBack()
    }
    
    companion object {
        const val MAP_PREFERENCE_NAMESPACE = "SHARED_MAPBOX_LAYER_PREFERENCES"

        fun newInstance(collectItem: CollectItem): Fragment {
            return CollectDetailsFragment(collectItem)
        }
    }
}
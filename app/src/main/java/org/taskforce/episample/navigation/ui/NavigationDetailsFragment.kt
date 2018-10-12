package org.taskforce.episample.navigation.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import kotlinx.android.synthetic.main.fragment_navigation_details.*
import org.taskforce.episample.BuildConfig
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.managers.MapboxItemMarkerManager
import org.taskforce.episample.collection.ui.CollectAddFragment
import org.taskforce.episample.collection.ui.CollectDetailItemAdapter
import org.taskforce.episample.collection.ui.CollectGpsPrecisionViewModel
import org.taskforce.episample.collection.ui.ViewPhotoFragment
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.NavigationItem
import org.taskforce.episample.databinding.FragmentNavigationDetailsBinding
import org.taskforce.episample.utils.getCompatColor
import org.taskforce.episample.utils.toMapboxLatLng
import javax.inject.Inject

@SuppressLint("ValidFragment")
class NavigationDetailsFragment(private val navigationItem: NavigationItem) : Fragment(), MapboxMap.OnMarkerClickListener {

    @Inject
    lateinit var config: Config

    lateinit var viewModel: NavigationDetailsViewModel
    lateinit var toolbarViewModel: NavigationToolbarViewModel
    lateinit var gpsViewModel: CollectGpsPrecisionViewModel
    lateinit var collectIconFactory: CollectIconFactory
    lateinit var detailItemAdapter: CollectDetailItemAdapter
    private val markerManagerLiveData = MutableLiveData<MapboxItemMarkerManager>()

    private val mapPreferences: SharedPreferences
        get() = requireActivity().getSharedPreferences(CollectAddFragment.MAP_PREFERENCE_NAMESPACE, Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(requireContext(), BuildConfig.MAPBOX_ACCESS_TOKEN)

        (requireActivity().application as EpiApplication).collectComponent?.inject(this)

        viewModel = ViewModelProviders.of(this@NavigationDetailsFragment,
                NavigationDetailsViewModelFactory(requireActivity().application,
                        navigationItem,
                        requireContext().getCompatColor(R.color.nav_incomplete),
                        requireContext().getCompatColor(R.color.nav_complete),
                        requireContext().getCompatColor(R.color.nav_skipped),
                        requireContext().getCompatColor(R.color.nav_problem)) {

                    val fragment = SurveyStatusDialogFragment.newInstance(it.id
                            ?: "", it.surveyStatus)
                    fragment.setTargetFragment(this, NavigationFragment.GET_SURVEY_STATUS_REASON_CODE)
                    fragment.show(requireFragmentManager(), SurveyStatusDialogFragment.TAG)
                })
                .get(NavigationDetailsViewModel::class.java)

        val vm = CollectGpsPrecisionViewModel(config.userSettings.gpsMinimumPrecision,
                config.userSettings.gpsPreferredPrecision,
                requireContext().getCompatColor(R.color.colorError),
                requireContext().getCompatColor(R.color.colorWarning),
                requireContext().getCompatColor(R.color.gpsAcceptable))
        gpsViewModel = vm

        collectIconFactory = CollectIconFactory(requireContext().resources)

        detailItemAdapter = CollectDetailItemAdapter(collectIconFactory)
        detailItemAdapter.resources = requireContext().resources

        toolbarViewModel = ViewModelProviders.of(this@NavigationDetailsFragment,
                NavigationToolbarViewModelFactory(requireActivity().application,
                        R.string.nav_detail_title,
                        config.enumerationSubject.singular)).get(NavigationToolbarViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val mapFragment = SupportMapFragment()

        val binding = DataBindingUtil.inflate<FragmentNavigationDetailsBinding>(inflater, R.layout.fragment_navigation_details,
                container,
                false).apply {
            childFragmentManager.beginTransaction()
                    .replace(R.id.navDetailsMap, mapFragment)
                    .commit()

            mapFragment.getMapAsync {
                markerManagerLiveData.postValue(MapboxItemMarkerManager(requireContext(), mapPreferences, it))

                it.setOnMarkerClickListener(this@NavigationDetailsFragment)
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(navigationItem.location.toMapboxLatLng(), CollectViewModel.zoomLevel))
            }

            navDetailFields.layoutManager = LinearLayoutManager(requireContext())
            navDetailFields.adapter = detailItemAdapter
        }

        binding.vm = viewModel
        binding.gpsVm = gpsViewModel
        binding.toolbarVm = toolbarViewModel
        binding.setLifecycleOwner(this)

        viewModel.customFields.observe(this, Observer {
            it?.let { fields ->
                detailItemAdapter.resources = resources
                detailItemAdapter.data = it.toMutableList()
                detailItemAdapter.notifyDataSetChanged()
            }
        })

        LiveDataPair(markerManagerLiveData, viewModel.data).observe(this, Observer {
            it?.let { (markerManager, navigationItem) ->
                markerManager.addMarkerDiff(listOf(navigationItem))
            }
        })

        gpsViewModel.precision.set(navigationItem.gpsPrecision)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar?.setNavigationOnClickListener {
            if (fragmentManager?.backStackEntryCount ?: 0 > 0) {
                fragmentManager?.popBackStack()
            } else {
                requireActivity().finish()
            }
        }

        navDetailsImage.setOnClickListener {
            navigationItem.image?.let { imageUri ->
                val photoFragment = ViewPhotoFragment.newInstance(imageUri)
                photoFragment.show(requireFragmentManager(), ViewPhotoFragment::class.java.simpleName)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            NavigationFragment.GET_SURVEY_STATUS_REASON_CODE -> {
                viewModel
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        return true
    }

    companion object {
        fun newInstance(navigationItem: NavigationItem): Fragment {
            return NavigationDetailsFragment(navigationItem)
        }
    }
}
package org.taskforce.episample.sync.ui

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.taskforce.episample.R
import org.taskforce.episample.databinding.FragmentEnumeratorSyncBinding

class EnumeratorSyncFragment: Fragment() {

    private lateinit var viewModel: EnumeratorSyncViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(EnumeratorSyncViewModel::class.java)
        lifecycle.addObserver(viewModel.directTransferService)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentEnumeratorSyncBinding>(inflater, R.layout.fragment_enumerator_sync, container, false)

        binding.vm = viewModel
        binding.setLifecycleOwner(this)

        return binding.root
    }

    companion object {
        fun newInstance(): EnumeratorSyncFragment {
            return EnumeratorSyncFragment()
        }
    }
}

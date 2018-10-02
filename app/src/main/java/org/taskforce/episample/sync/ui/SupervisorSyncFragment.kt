package org.taskforce.episample.sync.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_supervisor_sync.*
import org.taskforce.episample.R
import org.taskforce.episample.collection.ui.CollectFragment

class SupervisorSyncFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_supervisor_sync, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_supervisor_sync_sendStudyButton.setOnClickListener({
            requireFragmentManager()
                    .beginTransaction()
                    .replace(R.id.configFrame, ShareStudyFragment.newInstance())
                    .addToBackStack(ShareStudyFragment::class.java.name)
                    .commit()
        })

        fragment_supervisor_sync_syncWithEnumerators.setOnClickListener({
            requireFragmentManager()
                    .beginTransaction()
                    .replace(R.id.configFrame, SyncWithEnumeratorFragment.newInstance())
                    .addToBackStack(SyncWithEnumeratorFragment::class.java.name)
                    .commit()
        })
    }

    companion object {
        fun newInstance(): Fragment {
            return SupervisorSyncFragment()
        }
    }
}

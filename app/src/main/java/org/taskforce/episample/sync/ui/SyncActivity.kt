package org.taskforce.episample.sync.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.taskforce.episample.R

class SyncActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val fragment = if (intent.getBooleanExtra(EXTRA_IS_SUPERVISOR, false)) {
            SupervisorSyncFragment.newInstance()
        } else {
            EnumeratorSyncFragment.newInstance()
        }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.configFrame, fragment)
                .commit()
    }

    companion object {
        private const val EXTRA_IS_SUPERVISOR = "EXTRA_IS_SUPERVISOR"

        fun startActivity(context: Context, isSupervisor: Boolean) {
            val intent = Intent(context, SyncActivity::class.java)

            intent.putExtra(EXTRA_IS_SUPERVISOR, isSupervisor)

            context.startActivity(intent)
        }
    }
}
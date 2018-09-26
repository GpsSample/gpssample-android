package org.taskforce.episample.sync.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigStartFragment

class ShareStudyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.configFrame, ShareStudyFragment())
                .commit()
    }


    companion object {
        const val TAG = "ShareStudyActivity"
        private const val EXTRA_IS_SERVER = "EXTRA_IS_SERVER"

        fun startActivity(context: Context, isServer: Boolean) {
            val intent = Intent(context, ShareStudyActivity::class.java)

            intent.putExtra(EXTRA_IS_SERVER, isServer)

            context.startActivity(intent)
        }
    }
}
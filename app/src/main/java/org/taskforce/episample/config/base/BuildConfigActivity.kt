package org.taskforce.episample.config.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import org.taskforce.episample.R

class BuildConfigActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_build_config)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.configFrame, ConfigFragment.newInstance())
                .commit()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, BuildConfigActivity::class.java))
        }
    }
}
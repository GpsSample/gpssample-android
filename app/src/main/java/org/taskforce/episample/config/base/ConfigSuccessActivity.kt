package org.taskforce.episample.config.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import org.taskforce.episample.R

class ConfigSuccessActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_success)

        val config = intent.getSerializableExtra(EXTRA_CONFIG) as Config

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.configFrame, ConfigSuccessFragment.newInstance(config))
                .commit()
    }

    companion object {
        private const val EXTRA_CONFIG = "EXTRA_CONFIG"

        fun startActivity(context: Context, config: Config) {
            val intent = Intent(context, ConfigSuccessActivity::class.java)
            intent.putExtra(EXTRA_CONFIG, config)
            context.startActivity(intent)
        }
    }
}
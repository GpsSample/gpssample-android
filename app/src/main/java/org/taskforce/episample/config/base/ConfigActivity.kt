package org.taskforce.episample.config.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import org.taskforce.episample.R

class ConfigActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.configFrame, ConfigStartFragment())
                .commit()
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, ConfigActivity::class.java)
            context.startActivity(intent)
        }
    }
}
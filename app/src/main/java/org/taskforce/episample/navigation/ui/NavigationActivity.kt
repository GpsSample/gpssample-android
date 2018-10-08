package org.taskforce.episample.navigation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.taskforce.episample.R

class NavigationActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentFrame, NavigationListFragment.newInstance())
                .commit()
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, NavigationActivity::class.java)
            context.startActivity(intent)
        }
    }
}

package org.taskforce.episample.navigation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import org.taskforce.episample.R

class NavigationActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        val navigationPlanId = intent.getStringExtra(EXTRA_NAVIGATION_PLAN_ID)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentFrame, NavigationPlanFragment.newInstance(navigationPlanId))
                .commit()
    }

    companion object {
        const val EXTRA_NAVIGATION_PLAN_ID = "EXTRA_NAVIGATION_PLAN_ID"

        fun startActivity(context: Context, navigationPlanId: String) {
            val intent = Intent(context, NavigationActivity::class.java)
            intent.putExtra(EXTRA_NAVIGATION_PLAN_ID, navigationPlanId)
            context.startActivity(intent)
        }
    }
}

package org.taskforce.episample.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R

class LoginActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.loginFrame, LoginFragment.newInstance())
                .commit()
    }

    override fun finish() {
        (application as EpiApplication).clearCollectComponent()
        super.finish()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    }
}
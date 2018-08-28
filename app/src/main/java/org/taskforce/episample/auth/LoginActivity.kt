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

        val configId = intent.getStringExtra(ARG_CONFIG_ID)!!
        val studyId = intent.getStringExtra(ARG_STUDY_ID)!!

        setContentView(R.layout.activity_login)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.loginFrame, LoginFragment.newInstance(configId, studyId))
                .commit()
    }

    companion object {
        const val ARG_CONFIG_ID = "ARG_CONFIG_ID"
        const val ARG_STUDY_ID = "ARG_STUDY_ID"
        fun startActivity(context: Context, configId: String, studyId: String) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(ARG_CONFIG_ID, configId)
            intent.putExtra(ARG_STUDY_ID, studyId)
            context.startActivity(intent)
        }
    }
}
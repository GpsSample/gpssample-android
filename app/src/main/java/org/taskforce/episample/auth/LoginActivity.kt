package org.taskforce.episample.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import org.taskforce.episample.R

class LoginActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val configId = intent.getStringExtra(EXTRA_CONFIG_ID)!!
        val studyId = intent.getStringExtra(EXTRA_STUDY_ID)!!

        setContentView(R.layout.activity_login)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.loginFrame, LoginFragment.newInstance(configId, studyId))
                .commit()
    }

    companion object {
        const val EXTRA_CONFIG_ID = "EXTRA_CONFIG_ID"
        const val EXTRA_STUDY_ID = "EXTRA_STUDY_ID"

        fun startActivity(context: Context, configId: String, studyId: String) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(EXTRA_CONFIG_ID, configId)
            intent.putExtra(EXTRA_STUDY_ID, studyId)
            context.startActivity(intent)
        }
    }
}
package org.taskforce.episample.permissions.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import org.taskforce.episample.R

class PermissionsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)
        supportFragmentManager.beginTransaction()
                 .replace(R.id.permissionsFrame, PermissionsFragment()).commit()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, PermissionsActivity::class.java))
        }
    }
}
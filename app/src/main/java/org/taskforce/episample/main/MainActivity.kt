package org.taskforce.episample.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.mainFrame, MainFragment.newInstance())
                .commit()
    }

    override fun finish() {
        (application as EpiApplication).clearCollectComponent()
        super.finish()
    }


    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}
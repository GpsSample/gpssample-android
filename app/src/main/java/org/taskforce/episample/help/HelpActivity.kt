package org.taskforce.episample.help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_help.*
import org.taskforce.episample.R

class HelpActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val path = intent.extras.getString(EXTRA_HELP_URL_PATH)
        activity_help_webView.loadUrl(path)
    }

    companion object {
        private const val EXTRA_HELP_URL_PATH = "EXTRA_HELP_URL_PATH"

        fun startActivity(context: Context, helpUrlPath: String) {
            val intent = Intent(context, HelpActivity::class.java)

            intent.putExtra(EXTRA_HELP_URL_PATH, helpUrlPath)

            context.startActivity(intent)
        }
    }

}
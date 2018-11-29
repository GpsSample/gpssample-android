package org.taskforce.episample.help

import android.content.Context
import android.content.Intent
import android.net.Uri

class HelpUtil {

    companion object {
        const val DEFAULT_URL = "https://github.com/EpiSample/episample-android/wiki/Welcome"
        fun startHelpActivity(context: Context, helpUrlPath: String? = DEFAULT_URL) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(helpUrlPath))
            context.startActivity(intent)
        }
    }
}
package org.taskforce.episample.sync.core

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import android.util.Log
import org.taskforce.episample.core.util.FileUtil
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */

class FileTransferService: IntentService("FileTransferService") {

    override fun onHandleIntent(intent: Intent?) {

        val context = applicationContext
        if (intent!!.action == ACTION_SEND_FILE) {
            val fileUri = intent.extras!!.getString(EXTRAS_FILE_PATH)
            val host = intent.extras!!.getString(EXTRAS_GROUP_OWNER_ADDRESS)
            val socket = Socket()
            val port = intent.extras!!.getInt(EXTRAS_GROUP_OWNER_PORT)

            try {
                Log.d(TAG, "Opening client socket - ")
                socket.bind(null)
                socket.connect(InetSocketAddress(host!!, port), SOCKET_TIMEOUT)

                Log.d(TAG, "Client socket - " + socket.isConnected)
                val stream = socket.getOutputStream()
                val cr = context.contentResolver
                var inputStream: InputStream? = null
                try {
                    inputStream = cr.openInputStream(Uri.parse(fileUri))
                    FileUtil.copyFile(inputStream, stream)
                } catch (e: FileNotFoundException) {
                    Log.d(TAG, e.toString())
                }

                Log.d(TAG, "Client: Data written")
            } catch (e: IOException) {
                Log.e(TAG, e.message)
            } finally {
                if (socket != null) {
                    if (socket.isConnected) {
                        try {
                            socket.close()
                            Log.d(TAG, "Socket Closed")
                        } catch (e: IOException) {
                            // Give up
                            e.printStackTrace()
                        }

                    }
                }
            }

        }
    }

    companion object {
        const val TAG = "FileTransferService"
        private const val SOCKET_TIMEOUT = 5000
        const val ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE"
        const val EXTRAS_FILE_PATH = "file_url"
        const val EXTRAS_GROUP_OWNER_ADDRESS = "go_host"
        const val EXTRAS_GROUP_OWNER_PORT = "go_port"
    }
}


package org.taskforce.episample.sync.core

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import org.taskforce.episample.core.util.FileUtil
import org.taskforce.episample.core.util.SocketUtil
import org.taskforce.episample.db.StudyRoomDatabase
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */

class ReceiveFileTransferService : IntentService("FileTransferService") {

    val context: Context
        get() = applicationContext

    val dbFolder: File
        get() = context.getDatabasePath("any").parentFile

    override fun onHandleIntent(intent: Intent?) {
        if (intent!!.action == ACTION_RECEIVE_FILE) {
            val host = intent.extras!!.getString(EXTRAS_GROUP_OWNER_ADDRESS)
            val port = intent.extras!!.getInt(EXTRAS_GROUP_OWNER_PORT)


            try {
                Socket(host, port).use { socket ->
                    socket.getOutputStream().use { out ->
                        socket.getInputStream().use { inputStream ->

                            val incomingZipFile = File(context.filesDir.absolutePath + "/wifip2pshared-" + System.currentTimeMillis()
                                    + ".zip")
                            SocketUtil.writeSocketToFile(inputStream, incomingZipFile)
                            FileUtil.unzip(incomingZipFile.absoluteFile, dbFolder)

                            val imagesZip = File(dbFolder.absolutePath + "/images.zip")
                            FileUtil.unzipImages(context, imagesZip)

                            val targetDatabase = StudyRoomDatabase.getDatabase(application)
                            val sourceDatabase = StudyRoomDatabase.reloadIncomingInstance(application)

                            val sourceDao = sourceDatabase.transferDao()
                            val targetDao = targetDatabase.transferDao()

                            targetDao.transfer(
                                    sourceDao.getEnumerations(),
                                    sourceDao.getLandmarks(),
                                    sourceDao.getBreadcrumbs(),
                                    sourceDao.getCustomFieldValues(),
                                    sourceDao.getNavigationPlans(),
                                    sourceDao.getNavigationItems(),
                                    sourceDao.getSamples(),
                                    sourceDao.getSampleEnumerations(),
                                    sourceDao.getSampleWarnings()
                            )

                            val outgoingZipFile = FileUtil.writeDatabaseToZip(context)

                            SocketUtil.writeFileToSocketStream(outgoingZipFile, out)
                        }
                    }
                }
            } catch (e: UnknownHostException) {
                System.err.println("Don't know about host $host")
                System.exit(1)
            } catch (e: IOException) {
                System.err.println("Couldn't get I/O for the connection to $host")
                System.exit(1)
            } catch (e: Exception) {
                // TODO don't catch everything
                Log.d(TAG, e.localizedMessage)
            }
        }
    }

    companion object {
        const val TAG = "FileTransferService"
        private const val SOCKET_TIMEOUT = 5000
        const val ACTION_RECEIVE_FILE = "org.taskforce.episample.RECEIVE_FILE"
        const val EXTRAS_GROUP_OWNER_ADDRESS = "go_host"
        const val EXTRAS_GROUP_OWNER_PORT = "go_port"
    }
}

class SendFileTransferService : IntentService("FileTransferService") {

    val context: Context
        get() = applicationContext


    override fun onHandleIntent(intent: Intent?) {
        if (intent!!.action == ACTION_SEND_FILE) {
            val fileUri = intent.extras!!.getString(EXTRAS_FILE_PATH)
            val host = intent.extras!!.getString(EXTRAS_GROUP_OWNER_ADDRESS)
            val socket = Socket()
            val port = intent.extras!!.getInt(EXTRAS_GROUP_OWNER_PORT)

            try {
                socket.bind(null)
                socket.connect(InetSocketAddress(host!!, port), SOCKET_TIMEOUT)

                val stream = socket.getOutputStream()
                val cr = context.contentResolver
                var inputStream: InputStream? = null
                try {
                    inputStream = cr.openInputStream(Uri.parse(fileUri))
                    FileUtil.copyFile(inputStream, stream)
                } catch (e: FileNotFoundException) {
                    Log.d(TAG, e.toString())
                }
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
        const val ACTION_SEND_FILE = "org.taskforce.episample.SEND_FILE"
        const val EXTRAS_FILE_PATH = "file_url"
        const val EXTRAS_GROUP_OWNER_ADDRESS = "go_host"
        const val EXTRAS_GROUP_OWNER_PORT = "go_port"
    }
}


package org.taskforce.episample.sync

import android.os.AsyncTask
import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.taskforce.episample.core.util.FileUtil
import org.taskforce.episample.sync.core.StudyDatabaseFilesChangedMessage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.ServerSocket


/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 *
 * **Important**
 * Call with `executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ...)` to
 * prevent blocking other operations while waiting for socket to close
 *
 */
class FileServerAsyncTask : AsyncTask<File, Void, String>() {

    override fun doInBackground(vararg params: File): String? {

        try {
            val serverSocket = ServerSocket(8988)
            Log.d(TAG, "Server: Socket opened")
            val client = serverSocket.accept()
            Log.d(TAG, "Server: connection done")


            val f = File(params[0].absolutePath + "/wifip2pshared-" + System.currentTimeMillis()
                    + ".zip")

            val dirs = File(f.parent)
            if (!dirs.exists())
                dirs.mkdirs()
            f.createNewFile()

            Log.d(TAG, "server: copying files " + f.toString())
            val inputstream = client.getInputStream()
            FileUtil.copyFile(inputstream, FileOutputStream(f))
            serverSocket.close()

            val dbFolder = params[1].parent

            FileUtil.delete(listOf(
                    File("$dbFolder/study_database"),
                    File("$dbFolder/study_database-shm"),
                    File("$dbFolder/study_database-wal")
            ))
            FileUtil.unzip(f.absoluteFile, File(dbFolder))
            EventBus.getDefault().post(StudyDatabaseFilesChangedMessage())

            return f.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, e.message)
            return null
        }

    }

    companion object {
        const val TAG = "FileServerAsyncTask"
    }
}
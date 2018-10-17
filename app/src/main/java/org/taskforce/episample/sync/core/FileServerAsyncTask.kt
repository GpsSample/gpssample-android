package org.taskforce.episample.sync

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.taskforce.episample.core.util.FileUtil
import org.taskforce.episample.core.util.SocketUtil
import org.taskforce.episample.sync.core.DirectTransferService
import org.taskforce.episample.sync.core.StudyReceivedMessage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
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
class ReceiveStudyAsyncTask(private val weakContext: WeakReference<Context>, private val transferService: WeakReference<DirectTransferService>) : AsyncTask<File, ReceiveStudyAsyncTask.Progress, String>() {

    enum class Progress {
        NOT_STARTED,
        AWAITING_STUDY,
        PROCESSING_STUDY,
        SYNC_COMPLETE
    }

    override fun doInBackground(vararg params: File): String? {



        try {
            val context = weakContext.get() ?: return null
            val serverSocket = ServerSocket(8988)

            Log.d(TAG, "Server: Socket opened")
            publishProgress(Progress.AWAITING_STUDY)
            val client = serverSocket.accept()
            publishProgress(Progress.PROCESSING_STUDY)
            Log.d(TAG, "Server: connection done")
            val filesPath = context.filesDir.absolutePath
            val f = File(filesPath + "/wifip2pshared-" + System.currentTimeMillis()
                    + ".zip")

            val dirs = File(f.parent)
            if (!dirs.exists())
                dirs.mkdirs()
            f.createNewFile()

            Log.d(TAG, "server: copying files " + f.toString())
            val inputstream = client.getInputStream()
            FileUtil.copyFile(inputstream, FileOutputStream(f))
            serverSocket.close()

            val dbFolderPath = context.getDatabasePath("any").parent

            FileUtil.delete(listOf(
                    File("$dbFolderPath/study_database"),
                    File("$dbFolderPath/study_database-shm"),
                    File("$dbFolderPath/study_database-wal")
            ))
            FileUtil.unzip(f.absoluteFile, File(dbFolderPath))
            FileUtil.moveFile(File("$dbFolderPath/study_database_incoming"),
                    File("$dbFolderPath/study_database"))
            FileUtil.moveFile(File("$dbFolderPath/study_database_incoming-shm"),
                    File("$dbFolderPath/study_database-shm"))
            FileUtil.moveFile(File("$dbFolderPath/study_database_incoming-wal"),
                    File("$dbFolderPath/study_database-wal"))

            val imagesZip = File("$dbFolderPath/images.zip")
            if (imagesZip.exists()) {
                FileUtil.unzipImages(context, imagesZip)
            }

            EventBus.getDefault().post(StudyReceivedMessage())
            publishProgress(Progress.SYNC_COMPLETE)

            return f.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, e.message)
            return null
        }
    }

    override fun onProgressUpdate(vararg values: Progress?) {
        super.onProgressUpdate(*values)

        val progress = values[0] ?: Progress.NOT_STARTED
        transferService.get()?.receiveStudySyncStatus?.postValue(progress)
    }

    companion object {
        const val TAG = "ReceiveStudyAsyncTask"
    }
}

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 *
 * **Important**
 * Call with `executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ...)` to
 * prevent blocking other operations while waiting for socket to close
 *
 */

class EnumeratorSyncAsyncTask(private val weakContext: WeakReference<Context>, private val transferService: WeakReference<DirectTransferService>) : AsyncTask<File, EnumeratorSyncAsyncTask.Progress, String>() {

    enum class Progress {
        NOT_STARTED,
        AWAITING_CONNECTION,
        SENDING_ENUMERATIONS,
        AWAITING_BACKUP,
        PROCESSING_BACKUP,
        SYNC_COMPLETE
    }

    override fun doInBackground(vararg params: File): String? {
        val context = weakContext.get() ?: return null
        val dbFolder = context.getDatabasePath("any").parentFile
        publishProgress(Progress.AWAITING_CONNECTION)

        val port = 8988
        try {
            ServerSocket(port).use { serverSocket ->
                serverSocket.accept().use { clientSocket ->
                    clientSocket.getOutputStream().use { out ->
                        clientSocket.getInputStream().use { inputStream ->

                            val outgoingZipFile = FileUtil.writeDatabaseToZip(context)

                            SocketUtil.writeFileToSocketStream(outgoingZipFile, out)
                            publishProgress(Progress.AWAITING_BACKUP)

                            val incomingZipFile = File(context.filesDir.absolutePath + "/wifip2pshared-" + System.currentTimeMillis()
                                    + ".zip")
                            SocketUtil.writeSocketToFile(inputStream, incomingZipFile)

                            publishProgress(Progress.PROCESSING_BACKUP)

                            processBackup(incomingZipFile, dbFolder)

                            publishProgress(Progress.SYNC_COMPLETE)
                            EventBus.getDefault().post(StudyReceivedMessage())
                        }
                    }
                }
            }
        } catch (e: IOException) {
            println("Exception caught when trying to listen on port "
                    + port + " or listening for a connection")
            println(e.message)
        } catch (e: Exception) {
            println(e.message)
        }

        return null
    }

    override fun onProgressUpdate(vararg values: EnumeratorSyncAsyncTask.Progress?) {
        super.onProgressUpdate(*values)

        val progress = values[0] ?: EnumeratorSyncAsyncTask.Progress.NOT_STARTED
        transferService.get()?.enumeratorSyncStatus?.postValue(progress)
    }

    private fun processBackup(zipFile: File, dbFolder: File) {
        FileUtil.delete(listOf(
                File("$dbFolder/study_database_incoming"),
                File("$dbFolder/study_database_incoming-shm"),
                File("$dbFolder/study_database_incoming-wal")
        ))

        FileUtil.unzip(zipFile, dbFolder)

        FileUtil.moveFile(File("$dbFolder/study_database_incoming"),
                File("$dbFolder/study_database"))
        FileUtil.moveFile(File("$dbFolder/study_database_incoming-shm"),
                File("$dbFolder/study_database-shm"))
        FileUtil.moveFile(File("$dbFolder/study_database_incoming-wal"),
                File("$dbFolder/study_database-wal"))
1
        weakContext.get()?.let { context ->
            val imagesZip = File(dbFolder.absolutePath + "/images.zip")
            if (imagesZip.exists()) {
                FileUtil.unzipImages(context, imagesZip)
            }
        }
    }

    companion object {
        const val TAG = "EnumeratorSyncAsyncTask"
    }
}
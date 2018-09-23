package org.taskforce.episample.supervisor.upload.managers

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.drive.*
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


interface UploadManager {
    val totalFiles: LiveData<Int>
    val progress: LiveData<Int>
    
    fun uploadFiles(files: List<File>): Boolean // TODO: should this be a java `File` or something more generic?
}

class DriveUploadManager : UploadManager {
    lateinit var driveClient: DriveClient
    lateinit var driveResourceClient: DriveResourceClient
    lateinit var driveAccount: GoogleSignInAccount

    override val totalFiles = MutableLiveData<Int>().apply { value = 0 }
    var currentProgress = 0
    override val progress = MutableLiveData<Int>().apply { value = 0 }

    private fun createFolder(folderName: String, parentId: DriveId? = null): Task<DriveFolder> {
        return driveResourceClient.rootFolder.onSuccessTask { rootFolder ->
            val folderChangeSet = MetadataChangeSet.Builder()
                    .setTitle(folderName)
                    .setMimeType(DriveFolder.MIME_TYPE)
                    .setStarred(true)
                    .build()

            return@onSuccessTask driveResourceClient.createFolder(parentId?.asDriveFolder()
                    ?: rootFolder!!,
                    folderChangeSet)
        }
    }

    private fun searchForFolder(folderName: String): Task<MetadataBuffer> {
        val builder = Query.Builder()
        builder.addFilter(Filters.eq(SearchableField.TITLE, folderName))
        builder.addFilter(Filters.eq(SearchableField.TRASHED, false))
        builder.addFilter(Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
        val query = builder.build()

        return driveResourceClient.rootFolder.onSuccessTask { folder ->
            val rootFolder = folder!!

            return@onSuccessTask driveResourceClient.queryChildren(rootFolder, query)
        }
    }

    private fun uploadFilesWithParent(files: List<File>, folderId: DriveId): List<Task<DriveFile>> {
        val tasks = mutableListOf<Task<DriveFile>>()
        files.forEach { file ->
            tasks.add(uploadFileWithParent(file, folderId))
        }
        
        return tasks
    }

    private fun uploadFileWithParent(file: File, folderId: DriveId): Task<DriveFile> {
        val createContents = driveResourceClient.createContents()
        val writeFileTask = createContents.onSuccessTask { contents ->
            val outputStream = contents?.parcelFileDescriptor
            FileOutputStream(outputStream?.fileDescriptor).use { writer ->
                writer.write(file.readBytes())
            }

            val changeSet = MetadataChangeSet.Builder()
                    .setTitle(file.name)
                    .setMimeType("application/*")
                    .setStarred(true)
                    .build()
            
            return@onSuccessTask driveResourceClient.createFile(folderId.asDriveFolder(), changeSet, contents)
        }
        writeFileTask.addOnSuccessListener {
            // TODO: Update progress saying a file succeeded
            Log.d(TAG, "File uploaded successfully")
            currentProgress += 1
            progress.postValue(currentProgress)
        }
        return writeFileTask
    }

    private fun uploadFilesToDatedFolder(files: List<File>, parentFolderId: DriveId) {
        val folderName = SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(Date())
        createFolder(folderName, parentFolderId).addOnSuccessListener {
            val tasks = uploadFilesWithParent(files, it.driveId)
            val allTasks = Tasks.whenAll(tasks)
            allTasks.addOnSuccessListener {
                Log.d(TAG, "Upload complete")
            }
            allTasks.addOnFailureListener {
                Log.w(TAG, "Failed uploading file", it)
            }
        }
    }

    /*
    1. Search for the GPSSample folder at the root of the user's Google Drive
    2. If it doesn't exist, create it
    3. Using the driveId from either the newly created or found folder, upload the files to a new folder
     */
    override fun uploadFiles(files: List<File>): Boolean {
        totalFiles.postValue(files.size)
        currentProgress = 0
        progress.postValue(0)
        searchForFolder(APP_FOLDER).addOnSuccessListener {
            if (it.count == 0) { // Folder doesn't currently exist
                createFolder(APP_FOLDER).addOnSuccessListener {
                    uploadFilesToDatedFolder(files, it.driveId)
                }
            } else { // Folder exists
                val id = it[0].driveId
                uploadFilesToDatedFolder(files, id)
            }
        }

        // TODO: if successful, save the date as the most recent upload for use later

        return true
    }

    companion object {
        const val TAG = "DriveUploadManager"
        const val APP_FOLDER = "GPSSample"
    }
}

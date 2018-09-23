package org.taskforce.episample.supervisor.upload.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.support.v4.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.drive.Drive
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.supervisor.upload.managers.DriveUploadManager
import org.taskforce.episample.supervisor.upload.managers.UploadManager
import org.taskforce.episample.toolbar.managers.LanguageManager
import java.io.File
import javax.inject.Inject

class StudyUploadViewModel(application: Application,
                           private val performSignIn: () -> Unit): AndroidViewModel(application) {
    
    @Inject
    lateinit var uploadManager: UploadManager
    
    @Inject
    lateinit var languageManager: LanguageManager
    var languageService: LanguageService
    
    init {
        getApplication<EpiApplication>().component.inject(this)
        
        languageService = LanguageService(languageManager)
    }
    
    val titleText = MutableLiveData<String>().apply { value = languageService.getString(R.string.final_upload_title) }
    
    val messageText = MutableLiveData<String>().apply { value = languageService.getString(R.string.final_upload_message) }
    
    val buttonText = MutableLiveData<String>().apply { value = languageService.getString(R.string.final_upload_button) }
    
    val reportContentsTitle = MutableLiveData<String>().apply { value = languageService.getString(R.string.final_upload_report_contents) }
    
    // TODO: Get the last updated time
    val lastUpdated = MutableLiveData<String>().apply { value = languageService.getString(R.string.final_upload_last_updated, "TODO") }
    
    val uploadButtonEnabled = MutableLiveData<Boolean>().apply {
        value = true
    }
    
    val noWifiError = MutableLiveData<String>().apply { value = languageService.getString(R.string.final_upload_need_wifi) }
    
    val progress = uploadManager.progress
    
    val totalNumberOfFiles = uploadManager.totalFiles
    
    fun beginUpload() {
        performSignIn()
    }
    
    private fun uploadFile() {
        getClientInfo()
        
        val dbFiles = gatherDatabaseFiles()
        val imageFiles = gatherImages()
        
        uploadManager.uploadFiles(dbFiles)
    }
    
    fun continueUpload() {
        // TODO: Navigate to the upload progress screens where the upload will actually take place
        uploadFile()
    }

    private fun getClientInfo() {
        val account = GoogleSignIn.getLastSignedInAccount(getApplication<EpiApplication>().applicationContext)
        
        account?.let {
            val driveClient = Drive.getDriveClient(getApplication<EpiApplication>().applicationContext, account)
            val driveResourceClient = Drive.getDriveResourceClient(getApplication<EpiApplication>().applicationContext, account)
            (uploadManager as DriveUploadManager).driveAccount = account
            (uploadManager as DriveUploadManager).driveClient = driveClient
            (uploadManager as DriveUploadManager).driveResourceClient = driveResourceClient
        }
    }
    
    private fun gatherDatabaseFiles(): List<File> {
        val databaseName = "study_database"

        val db = getApplication<EpiApplication>().getDatabasePath(databaseName)
        val dbShm = getApplication<EpiApplication>().getDatabasePath("$databaseName-wal")
        val dbWal = getApplication<EpiApplication>().getDatabasePath("$databaseName-shm")
        
        return listOf(db, dbShm, dbWal)
    }
    
    private fun gatherImages(): List<File> {
        // TODO
        return listOf()
    }
}

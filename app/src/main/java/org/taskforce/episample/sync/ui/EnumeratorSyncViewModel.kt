package org.taskforce.episample.sync.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import org.taskforce.episample.R
import org.taskforce.episample.sync.EnumeratorSyncAsyncTask.Progress.*
import org.taskforce.episample.sync.core.DirectTransferService
import org.taskforce.episample.sync.core.DirectTransferServiceMode
import org.taskforce.episample.sync.core.LiveDirectTransferService

class EnumeratorSyncViewModel(application: Application): AndroidViewModel(application) {

    val directTransferService: DirectTransferService = LiveDirectTransferService(application, DirectTransferServiceMode.ENUMERATOR_SYNC)
    val deviceName = directTransferService.deviceNameLiveData
    val syncStatus: LiveData<Int> = Transformations.map(directTransferService.enumeratorSyncStatus) {
        when (it) {
            NOT_STARTED -> R.string.enumerator_sync_status_discoverable
            AWAITING_CONNECTION -> R.string.enumerator_sync_status_awaiting_connection
            SENDING_ENUMERATIONS -> R.string.enumerator_sync_status_sending_study_data
            AWAITING_BACKUP ->R.string.enumerator_sync_status_awaiting_study_data
            PROCESSING_BACKUP -> R.string.enumerator_sync_status_processing_study_data
            SYNC_COMPLETE -> R.string.enumerator_sync_status_complete
        }
    }

}
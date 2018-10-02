package org.taskforce.episample.sync.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import org.taskforce.episample.sync.core.DirectTransferService
import org.taskforce.episample.sync.core.DirectTransferServiceMode
import org.taskforce.episample.sync.core.LiveDirectTransferService

class SyncWithEnumeratorViewModel(application: Application): AndroidViewModel(application) {

    val directTransferService: DirectTransferService = LiveDirectTransferService(application, DirectTransferServiceMode.SUPERVISOR_SYNC)
}
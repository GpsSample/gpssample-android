package org.taskforce.episample.config.transfer

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class TransferDialogViewModelFactory(private val transferManager: TransferManager,
                                     private val bucketTarget: TransferFileBucket,
                                     val title: String,
                                     val hint: String,
                                     val cancel: String,
                                     val download: String,
                                     private val genericErrorMessage: String,
                                     private val dismiss: () -> Unit) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TransferDialogViewModel(transferManager,
                bucketTarget,
                title,
                hint,
                cancel,
                download,
                genericErrorMessage,
                dismiss) as T
    }

}
package org.taskforce.episample.config.transfer

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.webkit.URLUtil
import com.google.android.gms.common.util.IOUtils
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.taskforce.episample.R
import java.io.File

class TransferDialogViewModel(private val transferManager: TransferManager,
                              private val bucketTarget: TransferFileBucket,
                              val title: String,
                              val hint: String,
                              val cancel: String,
                              val download: String,
                              private val genericErrorMessage: String,
                              private val dismiss: () -> Unit) : ViewModel() {

    private var disposable: Disposable? = null

    var url = ObservableField("")
    var isLoading = ObservableField(false)

    var downloadEnabled = object: ObservableField<Boolean>(url, isLoading) {
        override fun get(): Boolean? =
                if (isLoading.get() == true) {
                    false
                } else {
                    url.get()?.trim()?.isNotEmpty() ?: false
                }
    }

    var downloadTextColor = object : ObservableField<Int>(downloadEnabled) {
        override fun get() =
                if (downloadEnabled.get() == true) {
                    R.color.colorAccent
                } else {
                    R.color.textColorDisabled
                }
    }

    var urlVisibility = object: ObservableField<Boolean>(isLoading) {
        override fun get(): Boolean? = isLoading.get() == false
    }

    var progressVisibility = object: ObservableField<Boolean>(isLoading) {
        override fun get(): Boolean? = isLoading.get() == true
    }

    var error = ObservableField("")

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }

    fun cancel() {
        dismiss()
    }

    fun download() {
        isLoading.set(true)
        error.set("")
        disposable = transferManager.transferFileService.downloadFile(url.get()!!)
                .subscribeOn(Schedulers.newThread())
                .subscribe({
                    IOUtils.copyStream(
                            it.byteStream(),
                            File(
                                    TransferFileService.constructFileLocation(
                                            bucketTarget,
                                            URLUtil.guessFileName(url.get()!!, null, null)
                                    )
                            ).outputStream()
                    )
                    isLoading.set(false)
                    dismiss()
                }, {
                    isLoading.set(false)
                    error.set(genericErrorMessage)
                })
    }
}
package org.taskforce.episample.config.transfer

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.support.v4.app.FragmentManager
import android.view.View
import android.widget.Toast
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.utils.bindDelegate

class TransferViewModel(
        languageService: LanguageService,
        transferManager: TransferManager,
        private val fragmentManager: FragmentManager,
        private val transferFileBucket: TransferFileBucket,
        private val uploadTitleRes: Int? = null,
        private val uploadExplanationTypeRes: Int? = null) : BaseObservable() {

    init {
        languageService.update = {
            fileListHeader = languageService.getString(uploadTitleRes
                    ?: R.string.config_transfer_files_title)
            fileListEmptyText = languageService.getString(R.string.config_list_empty)
            downloadButtonText = languageService.getString(R.string.config_transfer_button)
            uploadExplanation = languageService.getString(R.string.config_transfer_explanation,
                    languageService.getString(uploadExplanationTypeRes ?: R.string.config_transfer_default))
            fileListLoadingText = languageService.getString(R.string.config_transfer_receiving_files)
        }
    }

    @get:Bindable
    var fileListHeader by bindDelegate(languageService.getString(uploadTitleRes
            ?: R.string.config_transfer_files_title))

    @get:Bindable
    var fileListEmptyText by bindDelegate(languageService.getString(R.string.config_list_empty))

    @get:Bindable
    var downloadButtonText by bindDelegate(languageService.getString(R.string.config_transfer_button))

    @get:Bindable
    var uploadExplanation by bindDelegate(
            languageService.getString(R.string.config_transfer_explanation,
                    languageService.getString(uploadExplanationTypeRes
                            ?: R.string.config_transfer_default)))

    @get:Bindable
    var fileListLoadingText by bindDelegate(languageService.getString(R.string.config_transfer_receiving_files))

    @get:Bindable
    var uploadUrl: String? by bindDelegate(null)

    @get:Bindable
    var fileLoadingDisplayed by bindDelegate(false)

    @get:Bindable
    var showFileList by bindDelegate(true)

    @get:Bindable
    var fileListEmpty by bindDelegate(true)

    init {
        transferManager.transferStateObservable.subscribe {
            uploadUrl = "http://${it.ipAddress}"
        }
    }

    fun getFileFromUrl(view: View) {
        // TODO handle divergent behavior on 21-22 vs 23+
        Toast.makeText(view.context, "TODO", Toast.LENGTH_SHORT).show()
//        TransferDownloadDialogFragment().apply {
//            arguments = Bundle().apply {
//                putString(TransferFileBucket.TRANSFER_KEY, transferFileBucket.name)
//            }
//        }.show(fragmentManager, TransferDownloadDialogFragment::class.java.simpleName)
    }
}
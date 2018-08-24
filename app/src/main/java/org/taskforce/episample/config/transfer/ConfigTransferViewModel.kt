package org.taskforce.episample.config.transfer

import android.databinding.BaseObservable
import android.databinding.Bindable
import org.taskforce.episample.BR
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.utils.bindDelegate
import java.text.DecimalFormat


class ConfigTransferViewModel(
        languageService: LanguageService,
        private val transferViewModel: TransferViewModel,
        val transferTitle: String,
        val transferSize: Long,
        private val transferDate: String) : BaseObservable() {

    init {
        languageService.update = {
            created = languageService.getString(R.string.config_transfer_screen_created)
            cancel = languageService.getString(R.string.cancel)
        }
    }

    private var created = languageService.getString(R.string.config_transfer_screen_created)
        set(value) {
            field = value
            notifyPropertyChanged(BR.transferDetails)
        }

    private val displayTransferSize: String
        get() {
            val digit = (Math.log10(transferSize.toDouble()) / Math.log10(1024.0)).toInt()
            return "${DecimalFormat("#,##0.#")
                    .format(transferSize / digit)} ${transferUnits[digit]}"
        }

    @get:Bindable
    val transferPercentage: Int
        get() = (progress / transferSize).toInt()

    @get:Bindable
    val displayTransferPercentage: String
        get() = "$transferPercentage%"

    @get:Bindable
    var transferDetails by bindDelegate("$displayTransferSize, $created $transferDate")

    @get:Bindable
    var cancel by bindDelegate(languageService.getString(R.string.cancel))

    @get:Bindable
    var progress by bindDelegate(0, { _, _ ->
        notifyPropertyChanged(BR.inProgress)
        notifyPropertyChanged(BR.transferPercentage)
        notifyPropertyChanged(BR.displayTransferPercentage)
    })

    @get:Bindable
    var transferSuccessful by bindDelegate(languageService.getString(R.string.config_transfer_success))

    @get:Bindable
    var inProgress = false
        get() = progress != 0

    fun finish() {

    }

    companion object {
        private val transferUnits = arrayOf("B", "KB", "MB", "GB")
    }

}
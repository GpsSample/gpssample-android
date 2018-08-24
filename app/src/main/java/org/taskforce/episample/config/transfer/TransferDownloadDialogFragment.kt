package org.taskforce.episample.config.transfer

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.databinding.AppConfigDownloadDialogBinding
import javax.inject.Inject

class TransferDownloadDialogFragment : DialogFragment() {

    lateinit var viewModel: TransferDialogViewModel
    @Inject
    lateinit var transferManager: TransferManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            AppConfigDownloadDialogBinding.inflate(inflater).apply {
                arguments?.let {
                    viewModel = ViewModelProviders.of(this@TransferDownloadDialogFragment, TransferDialogViewModelFactory(transferManager,
                            TransferFileBucket.valueOf(it.getString(TransferFileBucket.TRANSFER_KEY)),
                            getString(R.string.config_transfer_dialog_title),
                            getString(R.string.config_transfer_dialog_hint),
                            getString(R.string.config_transfer_dialog_cancel),
                            getString(R.string.config_transfer_dialog_download),
                            getString(R.string.config_transfer_dialog_error)
                    ) {
                        dismiss()
                    }).get(TransferDialogViewModel::class.java)

                    vm = viewModel
                }
            }.root
}
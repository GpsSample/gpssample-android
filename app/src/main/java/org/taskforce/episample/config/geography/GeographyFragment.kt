package org.taskforce.episample.config.geography

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.geography.model.FeatureCollection
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.transfer.TransferFileBucket
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.config.transfer.TransferViewModel
import org.taskforce.episample.databinding.FragmentConfigGeographyBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject

class GeographyFragment : Fragment() {

    @Inject
    lateinit var transferManager: TransferManager

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var viewModel: GeographyViewModel

    lateinit var fileAdapter: EnumerationFileAdapter
    
    lateinit var configBuildViewModel: ConfigBuildViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        viewModel = ViewModelProviders.of(this@GeographyFragment.requireActivity(),
                GeographyViewModelFactory(LanguageService(languageManager),
                        EnumerationAreaAdapter(),
                        configBuildViewModel.configBuildManager))
                .get(GeographyViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentConfigGeographyBinding.inflate(inflater).apply {

            /**
             * When the quickstart block was included in the GeographyViewModelFactory, it was only
             * being created on the first instance of the Fragment. If you navigated away from the Fragment
             * and came back, a new Fragment instance was created but the ViewModel was returned the same
             * Singleton that still referenced the childFragmentManager from the first instance of
             * the Fragment. It needs to be reset every time you get the ViewModel.
             */
            viewModel.quickstart = showQuickStartDialog()
            viewModel.pickFile = pickFile()

            headerVm = ConfigHeaderViewModel(
                    LanguageService(languageManager),
                    R.string.config_geography_title,
                    R.string.config_geography_explanation)
            vm = viewModel
            transferVm = TransferViewModel(
                    LanguageService(languageManager),
                    transferManager,
                    childFragmentManager,
                    TransferFileBucket.ENUMERATION)
        }

        fileAdapter = EnumerationFileAdapter()
        binding.geographyFileList.adapter = fileAdapter

        return binding.root
    }

    private fun showQuickStartDialog() = {
        GeographyDialogFragment().show(childFragmentManager, GeographyDialogFragment::class.java.simpleName)
    }

    private fun pickFile() = {

        val openFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        openFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
        openFileIntent.type = "*/*" // TODO: Is there a better MIME type?
        startActivityForResult(openFileIntent, OPEN_FILE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == OPEN_FILE_CODE) {
            data?.let {
                val uri = it.data
                val file = File(uri.path)
                val fileText = readTextFromUri(uri)
                val featureCollection = convertJsonToFeatureCollection(fileText)
                Log.d(TAG, "${featureCollection?.features?.size ?: "Conversion failed"}")
                fileAdapter.data.add(file.name)
                fileAdapter.notifyDataSetChanged()
                viewModel.loadEnumerations(featureCollection)
            }
        }
    }

    private fun readTextFromUri(uri: Uri): String {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(
                inputStream))
        val stringBuilder = StringBuilder()
        var line = reader.readLine()
        while (line != null) {
            stringBuilder.append(line)
            line = reader.readLine()
        }
        inputStream.close()
        return stringBuilder.toString()

    }

    private fun convertJsonToFeatureCollection(jsonString: String?): FeatureCollection? {
        jsonString?.let {
            val mapper = jacksonObjectMapper()
            return try {
                mapper.readValue(jsonString, FeatureCollection::class.java)
            } catch (e: Exception) {
                null
            }

        } ?: return null
    }

    companion object {
        const val TAG = "GeographyFragment"
        const val HELP_TARGET = "#geography"

        const val OPEN_FILE_CODE = 23
    }
}
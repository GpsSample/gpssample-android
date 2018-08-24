package org.taskforce.episample.permissions.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigActivity
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentPermissionsBinding
import org.taskforce.episample.permissions.managers.PermissionManager
import org.taskforce.episample.permissions.viewmodels.PermissionsViewModel
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import org.taskforce.episample.utils.createSnackbar
import javax.inject.Inject

class PermissionsFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var permissionManager: PermissionManager

    private lateinit var viewModel: PermissionsViewModel
    private lateinit var toolbarViewModel: ToolbarViewModel

    private lateinit var languageDisposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)

        permissionManager.permissionsRequest = { permissions: Array<String> ->
            requestPermissions(permissions, PermissionManager.PERMISSIONS_REQUEST_CODE)
        }
        viewModel = PermissionsViewModel(
                LanguageService(languageManager),
                lifecycle,
                permissionManager, {
            ConfigActivity.startActivity(requireContext())
            requireActivity().finish()
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentPermissionsBinding.inflate(inflater).apply {
                vm = viewModel
                toolbarViewModel = ToolbarViewModel(
                        LanguageService(languageManager),
                        languageManager,
                        HELP_TARGET).apply {
                    title = languageManager.getString(R.string.permission_toolbar_title)
                    languageDisposable = languageManager.languageEventObservable.subscribe {
                        title = languageManager.getString(R.string.permission_toolbar_title)
                    }
                }
                toolbarVm = toolbarViewModel
                headerVm = ConfigHeaderViewModel(LanguageService(languageManager),
                        R.string.welcome_title,
                        R.string.permission_description)
            }.root

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionManager.PERMISSIONS_REQUEST_CODE -> {
                viewModel.onRequestPermissionsResult()
                if (!areAllPermissionsGranted(grantResults)) {
                    view?.createSnackbar(
                            languageManager.getString(R.string.alert_permission_not_all),
                            languageManager.getString(R.string.alert_permission_action),
                            { _: View ->
                                viewModel.next()
                            }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        languageDisposable.dispose()
        toolbarViewModel.onDestroy()

    }

    companion object {
        private const val HELP_TARGET = "#permissions"
    }
}

private fun areAllPermissionsGranted(grantResults: IntArray): Boolean =
        grantResults.reduce { acc, i -> acc + i } == 0
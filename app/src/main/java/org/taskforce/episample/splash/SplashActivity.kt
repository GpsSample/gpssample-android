package org.taskforce.episample.splash

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import org.taskforce.episample.BuildConfig
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.auth.LoginActivity
import org.taskforce.episample.config.base.ConfigActivity
import org.taskforce.episample.injection.CollectModule
import org.taskforce.episample.permissions.managers.PermissionManager
import org.taskforce.episample.permissions.ui.PermissionsActivity
import javax.inject.Inject


class SplashActivity : FragmentActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as EpiApplication).component.inject(this)

        viewModel = ViewModelProviders.of(this).get(SplashViewModel::class.java)

        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()

        Fabric.with(this, crashlyticsKit)

        viewModel.studyConfig.observe(this, Observer {
            if (permissionManager.allPermissions) {
                if (it != null) {
                    (application as EpiApplication)
                            .createCollectComponent(CollectModule(application,
                                    it.id, it.studyId!!))
                    LoginActivity.startActivity(this)
                } else {
                    ConfigActivity.startActivity(this)
                }
            } else {
                PermissionsActivity.startActivity(this)
            }
            finish()
        })
    }
}
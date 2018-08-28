package org.taskforce.episample.auth

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.widget.ArrayAdapter
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.ConfigManager
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.config.ResolvedConfig
import org.taskforce.episample.managers.LiveConfig
import org.taskforce.episample.utils.bindDelegate
import javax.inject.Inject

class LoginViewModel(application: Application,
                     languageService: LanguageService,
                     val configId: String,
                     val languageSelectAdapter: ArrayAdapter<String>,
                     private val signIn: (name: String, config: Config) -> Unit,
                     private val signInAsSupervisor: (name: String, config: Config) -> Unit,
                     private val displaysAdminLoginDialog: () -> Unit) : BaseObservable() {

    val configRepository = ConfigRepository(application)
    val configData = configRepository.getResolvedConfig(configId)

    val configObserver: Observer<ResolvedConfig> = Observer {

        config = it?.let { return@let LiveConfig(application, it!!) }
        adminPassword = it?.adminSettings?.password
    }

    var config: Config? = null
    var adminPassword: String? = null

    init {
        languageService.update = {
            welcome = languageService.getString(R.string.welcome_title)
            nameHint = languageService.getString(R.string.login_name_hint)
            supervisorSignInText = languageService.getString(R.string.login_signin_supervisor)
            passwordHint = languageService.getString(R.string.login_password_supervisor_hint)
        }

        configData.observeForever(configObserver)
    }

    @get:Bindable
    var welcome by bindDelegate(languageService.getString(R.string.welcome_title))

    @get:Bindable
    var nameHint by bindDelegate(languageService.getString(R.string.login_name_hint))

    @get:Bindable
    var name by bindDelegate<String?>(null)

    var adapter = languageSelectAdapter

    @get:Bindable
    var supervisorSignIn by bindDelegate(false)

    @get:Bindable
    var supervisorSignInText by bindDelegate(languageService.getString(R.string.login_signin_supervisor))

    @get:Bindable
    var password by bindDelegate<String?>(null)

    @get:Bindable
    var passwordHint by bindDelegate(languageService.getString(R.string.login_password_supervisor_hint))

    @get:Bindable
    var admin by bindDelegate(languageService.getString(R.string.login_admin))

    @get:Bindable
    var signInText by bindDelegate(languageService.getString(R.string.login_signin))

    fun signIn() {
        if (name == null) {
            //TODO: display error message that username cannot be null.
        } else {
            if (supervisorSignIn) {
                if (password != null) {
                    password?.let { password ->
                        name?.let {
                            if (password == adminPassword) {
                                signInAsSupervisor(it, config!!)
                            } else {
                                //TODO: display incorrect password error.
                            }
                        }
                    }
                } else {
                    //TODO: display supervisor password cannot be null.
                }
            } else {
                name?.let {
                    signIn(it, config!!)
                }
            }
        }
    }

    fun adminSignIn() {
        displaysAdminLoginDialog()
    }

    fun displaySupervisorSignIn() {
        supervisorSignIn = !supervisorSignIn
    }
}
package org.taskforce.episample.auth

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.widget.ArrayAdapter
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.LiveDataTriple
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.managers.LiveConfig

class LoginViewModel(application: Application,
                     languageService: LanguageService,
                     val configId: String,
                     val languageSelectAdapter: ArrayAdapter<String>,
                     private val signIn: (name: String, config: Config) -> Unit,
                     private val signInAsSupervisor: (name: String, config: Config) -> Unit,
                     private val displaysAdminLoginDialog: () -> Unit,
                     private val showIncorrectPassword: () -> Unit) : AndroidViewModel(application) {

    val studyRepository = StudyRepository(application)
    val configData = studyRepository.getResolvedConfig(configId)
    val studyData = studyRepository.getStudy()

    val studySupervisorPassword: String?
        get() {
            return studyData.value?.let {
                return@let it.password
            }
        }

    val config: Config?
        get() {
            return configData.value?.let {
                return@let LiveConfig(getApplication(), it)
            }
        }


    val welcome = languageService.getString(R.string.welcome_title)

    val nameHint = languageService.getString(R.string.login_name_hint)

    val name = MutableLiveData<String>().apply { value = "" }

    val adapter = languageSelectAdapter

    val supervisorSignIn = MutableLiveData<Boolean>().apply { value = false }

    val supervisorSignInText = languageService.getString(R.string.login_signin_supervisor)

    val password = MutableLiveData<String>().apply { value = "" }

    val passwordHint = languageService.getString(R.string.login_password_supervisor_hint)

    val admin = languageService.getString(R.string.login_admin)

    val signInText = languageService.getString(R.string.login_signin)

    val signinEnabled = (Transformations.map(LiveDataTriple(studyData, configData, name)) { (study, _, name) ->
        // At this point, configData is not null due to the return type of the repository
        return@map study != null && name.isNotBlank()
    } as MutableLiveData<Boolean>).apply { value = false }

    fun signIn() {
        if (name.value == null) {
            //TODO: display error message that username cannot be null.
        } else {
            supervisorSignIn.value?.let { supervisorSignIn ->
                config?.let { config ->
                    if (supervisorSignIn) {
                        if (password.value != null) {
                            password.value?.let { password ->
                                name.value?.let {
                                    if (password == studySupervisorPassword) {
                                        this@LoginViewModel.password.postValue("")
                                        signInAsSupervisor(it, config)
                                    } else {
                                        showIncorrectPassword()
                                    }
                                }
                            }
                        } else {
                            showIncorrectPassword()
                        }
                    } else {
                        name.value?.let {
                            signIn(it, config)
                        }
                    }
                }
            }
        }
    }

    fun adminSignIn() {
        displaysAdminLoginDialog()
    }

    fun displaySupervisorSignIn() {
        supervisorSignIn.value?.let {
            password.postValue("")
            supervisorSignIn.postValue(!it)
        }
    }
}
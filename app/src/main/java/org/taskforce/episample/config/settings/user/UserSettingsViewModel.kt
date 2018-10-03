package org.taskforce.episample.config.settings.user

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.support.v4.app.FragmentActivity
import android.view.View
import android.widget.ArrayAdapter
import org.taskforce.episample.R
import org.taskforce.episample.config.base.BaseConfigViewModel
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.settings.admin.AdminSettingsFragment
import org.taskforce.episample.toolbar.managers.LanguageManager

class UserSettingsViewModel(
        val photoCompressionAdapter: ArrayAdapter<String>,
        val photoCompressionSelection: () -> Int,
        val configBuildManager: ConfigBuildManager) : ViewModel(), BaseConfigViewModel {

    val gpsMinimumPrecision = object: ObservableField<String>("") {
        override fun set(value: String?) {
            super.set(value)

            validatePrecision()
        }
    }

    val gpsPreferredPrecision = object: ObservableField<String>("") {
        override fun set(value: String?) {
            super.set(value)
            
            validatePrecision()
        }
    }
    
    val minimumDistance = ObservableField("")

    val minimumDistanceEnforcement = ObservableField(false)

    val supervisorPasswordEnforcement = ObservableField(false)

    val supervisorPassword = ObservableField("")

    val isValid = object: ObservableField<Boolean>(gpsMinimumPrecision,
            gpsPreferredPrecision,
            minimumDistanceEnforcement,
            minimumDistance,
            supervisorPasswordEnforcement,
            supervisorPassword
    ) {
        override fun get(): Boolean? {

            val minPrecision = gpsMinimumPrecision.get()?.toDoubleOrNull()
            val preferredPrecision = gpsPreferredPrecision.get()?.toDoubleOrNull()
            return minPrecision != null &&
                    preferredPrecision != null &&
                    minPrecision >= preferredPrecision &&
                    (!minimumDistanceEnforcement.get()!! || (minimumDistanceEnforcement.get()!! && minimumDistance.get()?.toIntOrNull() != null)) &&
                    (!supervisorPasswordEnforcement.get()!! || (supervisorPasswordEnforcement.get()!! && supervisorPassword.get()?.isNotEmpty() == true))
        }
    }

    fun validatePrecision() {
        val preferredPrecision = gpsPreferredPrecision.get()?.toDoubleOrNull()
        val minPrecision = gpsMinimumPrecision.get()?.toDoubleOrNull()
        if (minPrecision != null && preferredPrecision != null) {
            if(minPrecision < preferredPrecision) {
                gpsMinimumPrecisionErrorEnabled.set(true)
                gpsMinimumPrecisionError.set(R.string.config_user_settings_gps_minimum_error)
            } else {
                gpsMinimumPrecisionError.set(LanguageManager.undefinedStringResourceId)
                gpsMinimumPrecisionErrorEnabled.set(false)
            }
        }
    }

    val collectionTitle = ObservableField(R.string.config_user_settings_collection_title)

    val gpsPreferredPrecisionHint = ObservableField(R.string.config_user_settings_gps_preferred_title)

    val gpsMinimumPrecisionHint = ObservableField(R.string.config_user_settings_gps_minimum_title)

    val gpsMinimumPrecisionError = ObservableField(LanguageManager.undefinedStringResourceId)
    
    val gpsMinimumPrecisionErrorEnabled = object: ObservableField<Boolean>(gpsMinimumPrecisionError) {
        override fun get() = gpsMinimumPrecisionError.get() != LanguageManager.undefinedStringResourceId
    }

    val gpsExplanation = ObservableField(R.string.config_user_settings_gps_explanation)

    val photoCompressionHint = ObservableField(R.string.config_user_settings_photo_compression_hint)

    val displacementEnforcementTitle = ObservableField(R.string.config_user_settings_distance_check)
    
    val displacementEnforcementSmallHint = ObservableField(R.string.config_user_settings_distance_small_hint)

    val photoEnforcementTitle = ObservableField(R.string.config_user_settings_photos)

    val photoEnforcement = ObservableField(false)

    val minimumDistanceHint = ObservableField(R.string.config_user_settings_minimum_distance)

    val requireCommentTitle = ObservableField(R.string.config_user_settings_comment)

    val requireComment = ObservableField(false)

    val requireCommentSmallHint = ObservableField(R.string.config_user_settings_comment_small_hint)

    val permissionsTitle = ObservableField(R.string.config_user_settings_permissions)

    val supervisorDisablePhotosTitle = ObservableField(R.string.config_user_settings_permissions_disable_photo)

    val supervisorDisablePhotos = ObservableField(false)

    val supervisorPasswordHint = ObservableField(R.string.config_user_settings_permissions_password)

    override val progress: Int
        get() = 7
    override val backEnabled: ObservableField<Boolean> = ObservableField(true)
    override val nextEnabled: ObservableField<Boolean> = isValid

    override fun onNextClicked(view: View) {
        configBuildManager.setUserSettings(
                UserSettings(
                        gpsMinimumPrecision.get()!!.toDouble(),
                        gpsPreferredPrecision.get()!!.toDouble(),
                        photoEnforcement.get()!!,
                        if (photoEnforcement.get()!!) {
                            photoCompressionSelection.invoke()
                        } else {
                            null
                        },
                        requireComment.get()!!,
                        minimumDistanceEnforcement.get()!!,
                        minimumDistance.get()?.toIntOrNull(),
                        supervisorDisablePhotos.get()!!,
                        supervisorPasswordEnforcement.get()!!,
                        supervisorPassword.get()!!
                )
        )
        val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
        fragmentManager
                .beginTransaction()
                .replace(R.id.configFrame, AdminSettingsFragment())
                .addToBackStack(AdminSettingsFragment::class.qualifiedName)
                .commit()
    }

    override fun onBackClicked(view: View) {
        val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
        fragmentManager.popBackStack()
    }

}
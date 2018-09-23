package org.taskforce.episample.config.base

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import org.taskforce.episample.config.fields.CustomFieldsFragment
import org.taskforce.episample.config.geography.GeographyFragment
import org.taskforce.episample.config.landmark.LandmarkFragment
import org.taskforce.episample.config.language.LanguageFragment
import org.taskforce.episample.config.name.ConfigNameFragment
import org.taskforce.episample.config.sampling.SamplingSelectionFragment
import org.taskforce.episample.config.sampling.SamplingSubsetFragment
import org.taskforce.episample.config.settings.admin.AdminSettingsFragment
import org.taskforce.episample.config.settings.display.DisplaySettingsFragment
import org.taskforce.episample.config.settings.server.ServerSettingsFragment
import org.taskforce.episample.config.settings.user.UserSettingsFragment
import org.taskforce.episample.config.survey.SurveyExportFragment

class ConfigAdapter(fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager) {


    enum class ChildFragmentType {
        NAME,
        UPLOAD,
        LANGUAGE,
        GEOGRAPHY,
        LANDMARK,
        CUSTOM_FIELDS,
        SAMPLING_SELECTION,
        SAMPLING_SUBSET,
        SURVEY_EXPORT,
        SERVER_SETTINGS,
        DISPLAY_SETTINGS,
        USER_SETTINGS,
        ADMIN_SETTINGS;

        fun fragmentClass(): Class<out Fragment> {
            when (this) {
                NAME -> {
                    return ConfigNameFragment::class.java
                }
                UPLOAD -> {
                    return ConfigUploadFragment::class.java
                }
                LANGUAGE -> {
                    return LanguageFragment::class.java
                }
                GEOGRAPHY -> {
                    return GeographyFragment::class.java
                }
                LANDMARK -> {
                    return LandmarkFragment::class.java
                }
                CUSTOM_FIELDS -> {
                    return CustomFieldsFragment::class.java
                }
                SAMPLING_SELECTION -> {
                    return SamplingSelectionFragment::class.java
                }
                SAMPLING_SUBSET -> {
                    return SamplingSubsetFragment::class.java
                }
                SURVEY_EXPORT -> {
                    return SurveyExportFragment::class.java
                }
                SERVER_SETTINGS -> {
                    return ServerSettingsFragment::class.java
                }
                DISPLAY_SETTINGS -> {
                    return DisplaySettingsFragment::class.java
                }
                USER_SETTINGS -> {
                    return UserSettingsFragment::class.java
                }
                ADMIN_SETTINGS -> {
                    return AdminSettingsFragment::class.java
                }
            }
        }

        fun helpTarget(): String {
            when (this) {
                NAME -> {
                    return ConfigNameFragment.HELP_TARGET
                }
                UPLOAD -> {
                    return ConfigUploadFragment.HELP_TARGET
                }
                LANGUAGE -> {
                    return LanguageFragment.HELP_TARGET
                }
                GEOGRAPHY -> {
                    return GeographyFragment.HELP_TARGET
                }
                LANDMARK -> {
                    return LandmarkFragment.HELP_TARGET
                }
                CUSTOM_FIELDS -> {
                    return CustomFieldsFragment.HELP_TARGET
                }
                SAMPLING_SELECTION -> {
                    return SamplingSelectionFragment.HELP_TARGET
                }
                SAMPLING_SUBSET -> {
                    return SamplingSubsetFragment.HELP_TARGET
                }
                SURVEY_EXPORT -> {
                    return SurveyExportFragment.HELP_TARGET
                }
                SERVER_SETTINGS -> {
                    return ServerSettingsFragment.HELP_TARGET
                }
                DISPLAY_SETTINGS -> {
                    return DisplaySettingsFragment.HELP_TARGET
                }
                USER_SETTINGS -> {
                    return UserSettingsFragment.HELP_TARGET
                }
                ADMIN_SETTINGS -> {
                    return AdminSettingsFragment.HELP_TARGET
                }
            }
        }
    }

    override fun getItem(position: Int) =
            fragmentTypes[position].fragmentClass().newInstance() ?: ConfigErrorFragment()

    override fun getCount() = fragmentTypes.size

    companion object {
        val fragmentTypes = listOf(
                ChildFragmentType.NAME,
//                ChildFragmentType.UPLOAD,
//                ChildFragmentType.LANGUAGE,
//                ChildFragmentType.GEOGRAPHY,
//                ChildFragmentType.LANDMARK,
                ChildFragmentType.CUSTOM_FIELDS,
//                ChildFragmentType.SAMPLING_SELECTION,
                ChildFragmentType.SAMPLING_SUBSET,
//                ChildFragmentType.SURVEY_EXPORT,
//                ChildFragmentType.SERVER_SETTINGS,
                ChildFragmentType.DISPLAY_SETTINGS,
                ChildFragmentType.USER_SETTINGS,
                ChildFragmentType.ADMIN_SETTINGS
        )

        val configFragmentMap: Map<Int, Class<out Fragment>>
        val configFragmentHelpMap: Map<Int, String>

        init {
            var mutableFragmentMap: MutableMap<Int, Class<out Fragment>> = mutableMapOf()
            var mutableFragmentHelpMap: MutableMap<Int, String> = mutableMapOf()
            fragmentTypes.forEachIndexed() { index, type ->
                mutableFragmentMap[index] = type.fragmentClass()
                mutableFragmentHelpMap[index] = type.helpTarget()
            }

            configFragmentMap = mutableFragmentMap
            configFragmentHelpMap = mutableFragmentHelpMap
        }
    }
}
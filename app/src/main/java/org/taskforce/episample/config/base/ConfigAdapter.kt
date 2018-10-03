package org.taskforce.episample.config.base

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.taskforce.episample.config.fields.CustomFieldsFragment
import org.taskforce.episample.config.geography.GeographyFragment
import org.taskforce.episample.config.landmark.LandmarkFragment
import org.taskforce.episample.config.language.LanguageFragment
import org.taskforce.episample.config.name.ConfigNameFragment
import org.taskforce.episample.config.sampling.SamplingGrouping
import org.taskforce.episample.config.sampling.SamplingSelectionFragment
import org.taskforce.episample.config.sampling.no_grouping.SamplingNoGroupFragment
import org.taskforce.episample.config.sampling.strata.SamplingStrataFragment
import org.taskforce.episample.config.sampling.subsets.SamplingSubsetFragment
import org.taskforce.episample.config.settings.admin.AdminSettingsFragment
import org.taskforce.episample.config.settings.display.DisplaySettingsFragment
import org.taskforce.episample.config.settings.server.ServerSettingsFragment
import org.taskforce.episample.config.settings.user.UserSettingsFragment
import org.taskforce.episample.config.survey.SurveyExportFragment

class ConfigAdapter(fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager) {

    enum class ChildFragmentType(val clazz: Class<out Fragment>) {
        NAME(ConfigNameFragment::class.java),
        UPLOAD(ConfigUploadFragment::class.java),
        LANGUAGE(LanguageFragment::class.java),
        GEOGRAPHY(GeographyFragment::class.java),
        LANDMARK(LandmarkFragment::class.java),
        CUSTOM_FIELDS(CustomFieldsFragment::class.java),
        SAMPLING_SELECTION(SamplingSelectionFragment::class.java),
        SAMPLING_SUBSET(SamplingSubsetFragment::class.java),
        SAMPLING_STRATA(SamplingStrataFragment::class.java),
        SURVEY_EXPORT(SurveyExportFragment::class.java),
        SERVER_SETTINGS(ServerSettingsFragment::class.java),
        DISPLAY_SETTINGS(DisplaySettingsFragment::class.java),
        USER_SETTINGS(UserSettingsFragment::class.java),
        ADMIN_SETTINGS(AdminSettingsFragment::class.java),
        ERROR(ConfigErrorFragment::class.java),
        SAMPLING_NO_GROUPS(SamplingNoGroupFragment::class.java);

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
                SAMPLING_STRATA -> {
                    return SamplingStrataFragment.HELP_TARGET
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
                SAMPLING_NO_GROUPS -> return SamplingNoGroupFragment.HELP_TARGET
                ERROR -> TODO()
            }
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (currentFragmentFlow[position]) {
            ConfigAdapter.ChildFragmentType.NAME -> ConfigNameFragment()
            ConfigAdapter.ChildFragmentType.UPLOAD -> ConfigUploadFragment()
            ConfigAdapter.ChildFragmentType.LANGUAGE -> LanguageFragment()
            ConfigAdapter.ChildFragmentType.GEOGRAPHY -> GeographyFragment()
            ConfigAdapter.ChildFragmentType.LANDMARK -> LandmarkFragment()
            ConfigAdapter.ChildFragmentType.CUSTOM_FIELDS -> CustomFieldsFragment()
            ConfigAdapter.ChildFragmentType.SAMPLING_SELECTION -> SamplingSelectionFragment()
            ConfigAdapter.ChildFragmentType.SAMPLING_SUBSET -> SamplingSubsetFragment()
            ConfigAdapter.ChildFragmentType.SURVEY_EXPORT -> SurveyExportFragment()
            ConfigAdapter.ChildFragmentType.SERVER_SETTINGS -> ServerSettingsFragment()
            ConfigAdapter.ChildFragmentType.DISPLAY_SETTINGS -> DisplaySettingsFragment()
            ConfigAdapter.ChildFragmentType.USER_SETTINGS -> UserSettingsFragment()
            ConfigAdapter.ChildFragmentType.ADMIN_SETTINGS -> AdminSettingsFragment()
            ConfigAdapter.ChildFragmentType.ERROR -> ConfigErrorFragment()
            ConfigAdapter.ChildFragmentType.SAMPLING_STRATA -> SamplingStrataFragment()
            ConfigAdapter.ChildFragmentType.SAMPLING_NO_GROUPS -> SamplingNoGroupFragment()
        }
    }

    override fun getCount() = currentFragmentFlow.size

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSamplingGroupingChanged(grouping: SamplingGrouping) {
        currentFragmentFlow = when (grouping) {
            SamplingGrouping.SUBSETS -> subsetFlow
            SamplingGrouping.STRATA -> strataFlow
            SamplingGrouping.NONE -> noGroupingFlow
        }
        notifyDataSetChanged()
    }

    override fun getItemPosition(obj: Any): Int {
        return PagerAdapter.POSITION_NONE
        //this will cause performance issues however, as of now I don't see a better way. Basically it causes the adapter to always instantiate a new item.
        // https://billynyh.github.io/blog/2014/03/02/fragment-state-pager-adapter/
    }

    companion object {
        var configFragmentMap: Map<Int, Class<out Fragment>>
        var configFragmentHelpMap: Map<Int, String>


        private val subsetFlow = listOf(
                ChildFragmentType.NAME,
                //                ChildFragmentType.UPLOAD,
//                ChildFragmentType.LANGUAGE,
                ChildFragmentType.GEOGRAPHY,
//                ChildFragmentType.LANDMARK,
                ChildFragmentType.CUSTOM_FIELDS,
                ChildFragmentType.SAMPLING_SELECTION,
                ChildFragmentType.SAMPLING_SUBSET,
//                ChildFragmentType.SURVEY_EXPORT,
//                ChildFragmentType.SERVER_SETTINGS,
                ChildFragmentType.DISPLAY_SETTINGS,
                ChildFragmentType.USER_SETTINGS,
                ChildFragmentType.ADMIN_SETTINGS
        )

        private val strataFlow = listOf(
                ChildFragmentType.NAME,
                //                ChildFragmentType.UPLOAD,
//                ChildFragmentType.LANGUAGE,
                ChildFragmentType.GEOGRAPHY,
//                ChildFragmentType.LANDMARK,
                ChildFragmentType.CUSTOM_FIELDS,
                ChildFragmentType.SAMPLING_SELECTION,
                ChildFragmentType.SAMPLING_STRATA,
//                ChildFragmentType.SURVEY_EXPORT,
//                ChildFragmentType.SERVER_SETTINGS,
                ChildFragmentType.DISPLAY_SETTINGS,
                ChildFragmentType.USER_SETTINGS,
                ChildFragmentType.ADMIN_SETTINGS
        )

        private val noGroupingFlow = listOf(
                ChildFragmentType.NAME,
                //                ChildFragmentType.UPLOAD,
//                ChildFragmentType.LANGUAGE,
                ChildFragmentType.GEOGRAPHY,
//                ChildFragmentType.LANDMARK,
                ChildFragmentType.CUSTOM_FIELDS,
                ChildFragmentType.SAMPLING_SELECTION,
                ChildFragmentType.SAMPLING_NO_GROUPS,
//                ChildFragmentType.SAMPLING_SUBSET,
//                ChildFragmentType.SURVEY_EXPORT,
//                ChildFragmentType.SERVER_SETTINGS,
                ChildFragmentType.DISPLAY_SETTINGS,
                ChildFragmentType.USER_SETTINGS,
                ChildFragmentType.ADMIN_SETTINGS
        )

        private var currentFragmentFlow = subsetFlow
            set(value) {
                field = value
                val mutableFragmentMap: MutableMap<Int, Class<out Fragment>> = mutableMapOf()
                val mutableFragmentHelpMap: MutableMap<Int, String> = mutableMapOf()
                currentFragmentFlow.forEachIndexed { index, type ->
                    mutableFragmentMap[index] = type.clazz
                    mutableFragmentHelpMap[index] = type.helpTarget()
                }

                configFragmentMap = mutableFragmentMap
                configFragmentHelpMap = mutableFragmentHelpMap
            }

        init {
            val mutableFragmentMap: MutableMap<Int, Class<out Fragment>> = mutableMapOf()
            val mutableFragmentHelpMap: MutableMap<Int, String> = mutableMapOf()
            currentFragmentFlow.forEachIndexed() { index, type ->
                mutableFragmentMap[index] = type.clazz
                mutableFragmentHelpMap[index] = type.helpTarget()
            }

            configFragmentMap = mutableFragmentMap
            configFragmentHelpMap = mutableFragmentHelpMap
        }
    }
}
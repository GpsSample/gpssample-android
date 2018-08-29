package org.taskforce.episample.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import org.taskforce.episample.R
import org.taskforce.episample.config.fields.CustomDropdown
import org.taskforce.episample.config.fields.CustomFieldTypeConstants
import org.taskforce.episample.core.interfaces.LandmarkTypeMetadata
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.GpsBreadcrumb
import org.taskforce.episample.db.collect.Landmark
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.metadata.*
import org.taskforce.episample.toolbar.managers.LanguageManager

fun Context.getLanguageManager() = LanguageManager(this)

fun Context.getCompatColor(color: Int) = ContextCompat.getColor(this, color)

val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)

val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

val Context.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

fun Resources.getResourceUri(res: Int): Uri =
     Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(getResourcePackageName(res))
            .appendPath(res.toString())
            .build()

val Activity.density: Float
    get() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.density
    }

fun View.closeKeyboard() {
    this.windowToken?.let {
        if (it.isBinderAlive) {
            (this.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)
                    ?.hideSoftInputFromWindow(it, 0)
        }
    }
}

fun View.createSnackbar(text: String, alertText: String, action: (v: View) -> Unit) {
    Snackbar.make(
            requireNotNull(this),
            text,
            Snackbar.LENGTH_LONG).apply {
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorError))
        view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
                .setTextColor(ContextCompat.getColor(context, R.color.textColorInverse))
        setActionTextColor(ContextCompat.getColor(context, R.color.textColorInverse))
        setAction(alertText, action)
    }.show()
}

val <K,V> Map<K,V>.inverse
    get() = entries.associateBy({ it.value }) { it.key }

fun org.taskforce.episample.config.fields.CustomField.makeDBConfig(configId: String): CustomField {
    var metadata: CustomFieldMetadata
    when (type) {
        CustomFieldType.NUMBER -> {
            metadata = NumberMetadata(isIntegerOnly = properties[CustomFieldTypeConstants.INTEGER_ONLY] as Boolean)
        }
        CustomFieldType.DATE -> {
            metadata = DateMetadata(showYear = properties[CustomFieldTypeConstants.YEAR] as Boolean,
                    showMonth = properties[CustomFieldTypeConstants.MONTH] as Boolean,
                    showDay = properties[CustomFieldTypeConstants.DAY] as Boolean,
                    showTime = properties[CustomFieldTypeConstants.TIME] as Boolean)
        }
        CustomFieldType.DROPDOWN -> {
            val dropdownSource = properties[CustomFieldTypeConstants.DROPDOWN_ITEMS] as List<CustomDropdown>
            val dropdownItems = dropdownSource.map {
                org.taskforce.episample.db.config.customfield.metadata.CustomDropdown(it.value, it.key)
            }
            metadata = DropdownMetadata(dropdownItems)
        }
        CustomFieldType.TEXT, CustomFieldType.CHECKBOX -> {
            metadata = EmptyMetadata()
        }
    }

    return CustomField(name,
            type,
            isAutomatic,
            isPrimary,
            isExported,
            isRequired,
            isPersonallyIdentifiableInformation,
            metadata,
            configId
    )
}

fun org.taskforce.episample.core.interfaces.Enumeration.toDBEnumeration(collectorName: String, studyId: String): Enumeration {
    return Enumeration(collectorName,
            location.latitude,
            location.longitude,
            note,
            isIncomplete,
            isExcluded,
            gpsPrecision,
            studyId,
            title,
            image,
            dateCreated)
}

fun org.taskforce.episample.core.interfaces.Breadcrumb.toDBBreadcrumb(collectorName: String, studyId: String): GpsBreadcrumb {
    return GpsBreadcrumb(gpsPrecision,
            collectorName,
            location.latitude,
            location.longitude,
            studyId
    )
}

fun org.taskforce.episample.core.interfaces.Landmark.toDBLandmark(collectorName: String, studyId: String): Landmark {
    return Landmark(
            title = title,
            lat = location.latitude,
            lng = location.longitude,
            studyId = studyId,
            note = note,
            image = image,
            builtInLandmark = (landmarkType.metadata as? LandmarkTypeMetadata.BuiltInLandmark)?.type,
            customLandmarkTypeId = (landmarkType.metadata as? LandmarkTypeMetadata.CustomId)?.id,
            gpsPrecision = gpsPrecision,
            dateCreated = dateCreated)
}
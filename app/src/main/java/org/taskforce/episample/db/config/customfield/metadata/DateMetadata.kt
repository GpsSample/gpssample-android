package org.taskforce.episample.db.config.customfield.metadata

import android.os.Parcel
import android.os.Parcelable
import org.taskforce.episample.db.config.customfield.CustomDateType

data class DateMetadata(val dateType: CustomDateType, val useCurrentTime: Boolean) : CustomFieldMetadata, Parcelable {
    constructor(parcel: Parcel) : this(
            CustomDateType.valueOf(parcel.readString()),
            parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(dateType.name)
        parcel.writeByte(if (useCurrentTime) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DateMetadata> {
        override fun createFromParcel(parcel: Parcel): DateMetadata {
            return DateMetadata(parcel)
        }

        override fun newArray(size: Int): Array<DateMetadata?> {
            return arrayOfNulls(size)
        }
    }

}
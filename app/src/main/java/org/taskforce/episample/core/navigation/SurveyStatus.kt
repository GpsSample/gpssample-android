package org.taskforce.episample.core.navigation

import android.os.Parcel
import android.os.Parcelable

sealed class SurveyStatus(): Parcelable {

    class Incomplete() : SurveyStatus() {
        constructor(parcel: Parcel) : this()

        override fun writeToParcel(parcel: Parcel, flags: Int) {
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Incomplete> {
            override fun createFromParcel(parcel: Parcel): Incomplete {
                return Incomplete(parcel)
            }

            override fun newArray(size: Int): Array<Incomplete?> {
                return arrayOfNulls(size)
            }
        }
    }

    class Complete() : SurveyStatus() {
        constructor(parcel: Parcel) : this()

        override fun writeToParcel(parcel: Parcel, flags: Int) {

        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Complete> {
            override fun createFromParcel(parcel: Parcel): Complete {
                return Complete(parcel)
            }

            override fun newArray(size: Int): Array<Complete?> {
                return arrayOfNulls(size)
            }
        }
    }

    class Problem(val reason: String): SurveyStatus() {
        constructor(parcel: Parcel) : this(parcel.readString())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(reason)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Problem> {
            override fun createFromParcel(parcel: Parcel): Problem {
                return Problem(parcel)
            }

            override fun newArray(size: Int): Array<Problem?> {
                return arrayOfNulls(size)
            }
        }
    }

    class Skipped(val reason: String): SurveyStatus() {
        constructor(parcel: Parcel) : this(parcel.readString())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(reason)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Skipped> {
            override fun createFromParcel(parcel: Parcel): Skipped {
                return Skipped(parcel)
            }

            override fun newArray(size: Int): Array<Skipped?> {
                return arrayOfNulls(size)
            }
        }
    }
}

package org.taskforce.episample.config.sampling.filter

import android.os.Parcel
import android.os.Parcelable
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.metadata.DateMetadata


sealed class CustomFieldForRules(val fieldId: String, val fieldName: String, val fieldType: CustomFieldType) : Parcelable {
    class TextField(fieldId: String, fieldName: String, fieldType: CustomFieldType) : CustomFieldForRules(fieldId, fieldName, fieldType), Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                CustomFieldType.valueOf(parcel.readString()))

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(fieldId)
            parcel.writeString(fieldName)
            parcel.writeString(fieldType.name)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<TextField> {
            override fun createFromParcel(parcel: Parcel): TextField {
                return TextField(parcel)
            }

            override fun newArray(size: Int): Array<TextField?> {
                return arrayOfNulls(size)
            }
        }
    }

    class BooleanField(fieldId: String, fieldName: String, fieldType: CustomFieldType) : CustomFieldForRules(fieldId, fieldName, fieldType), Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                CustomFieldType.valueOf(parcel.readString()))

        val values: List<String> = listOf(true.toString(), false.toString())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(fieldId)
            parcel.writeString(fieldName)
            parcel.writeString(fieldType.name)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<BooleanField> {
            override fun createFromParcel(parcel: Parcel): BooleanField {
                return BooleanField(parcel)
            }

            override fun newArray(size: Int): Array<BooleanField?> {
                return arrayOfNulls(size)
            }
        }

    }

    class IntegerField(fieldId: String, fieldName: String, fieldType: CustomFieldType) : CustomFieldForRules(fieldId, fieldName, fieldType), Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                CustomFieldType.valueOf(parcel.readString()))

        val values: List<String> = listOf(true.toString(), false.toString())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(fieldId)
            parcel.writeString(fieldName)
            parcel.writeString(fieldType.name)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<IntegerField> {
            override fun createFromParcel(parcel: Parcel): IntegerField {
                return IntegerField(parcel)
            }

            override fun newArray(size: Int): Array<IntegerField?> {
                return arrayOfNulls(size)
            }
        }
    }


    class DoubleField(fieldId: String, fieldName: String, fieldType: CustomFieldType) : CustomFieldForRules(fieldId, fieldName, fieldType), Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                CustomFieldType.valueOf(parcel.readString()))

        val values: List<String> = listOf(true.toString(), false.toString())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(fieldId)
            parcel.writeString(fieldName)
            parcel.writeString(fieldType.name)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<DoubleField> {
            override fun createFromParcel(parcel: Parcel): DoubleField {
                return DoubleField(parcel)
            }

            override fun newArray(size: Int): Array<DoubleField?> {
                return arrayOfNulls(size)
            }
        }
    }

    class DateField(fieldId: String, fieldName: String, fieldType: CustomFieldType, val metaData: DateMetadata) : CustomFieldForRules(fieldId, fieldName, fieldType), Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                CustomFieldType.valueOf(parcel.readString()),
                parcel.readParcelable<DateMetadata>(DateMetadata::class.java.classLoader))

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(fieldId)
            parcel.writeString(fieldName)
            parcel.writeString(fieldType.name)
            parcel.writeParcelable(metaData, flags)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<DateField> {
            override fun createFromParcel(parcel: Parcel): DateField {
                return DateField(parcel)
            }

            override fun newArray(size: Int): Array<DateField?> {
                return arrayOfNulls(size)
            }
        }

    }

    class DropdownField(fieldId: String, fieldName: String, fieldType: CustomFieldType, val values: List<Value>) : CustomFieldForRules(fieldId, fieldName, fieldType), Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                CustomFieldType.valueOf(parcel.readString()),
                parcel.createTypedArrayList(Value))

        class Value(val id: String, val name: String) : Parcelable {
            constructor(parcel: Parcel) : this(
                    parcel.readString(),
                    parcel.readString())

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(id)
                parcel.writeString(name)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<Value> {
                override fun createFromParcel(parcel: Parcel): Value {
                    return Value(parcel)
                }

                override fun newArray(size: Int): Array<Value?> {
                    return arrayOfNulls(size)
                }
            }
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(fieldId)
            parcel.writeString(fieldName)
            parcel.writeString(fieldType.name)
            parcel.writeTypedList(values)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<DropdownField> {
            override fun createFromParcel(parcel: Parcel): DropdownField {
                return DropdownField(parcel)
            }

            override fun newArray(size: Int): Array<DropdownField?> {
                return arrayOfNulls(size)
            }
        }
    }
}
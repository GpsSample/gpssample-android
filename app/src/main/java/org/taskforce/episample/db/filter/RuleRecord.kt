package org.taskforce.episample.db.filter

import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable
import org.taskforce.episample.config.sampling.SamplingMethodEntity
import org.taskforce.episample.config.sampling.filter.CustomFieldForRules
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.filter.checkbox.BooleanRuleFactory
import org.taskforce.episample.db.filter.date.DateRuleFactory
import org.taskforce.episample.db.filter.doubles.DoubleRuleFactory
import org.taskforce.episample.db.filter.dropdown.DropdownRuleFactory
import org.taskforce.episample.db.filter.integers.IntRuleFactory
import org.taskforce.episample.db.filter.text.TextRuleFactory
import java.io.Serializable
import java.util.*

@Entity(tableName = "rule_set_table", foreignKeys = [
    (ForeignKey(
            entity = SamplingMethodEntity::class, parentColumns = ["id"], childColumns = ["methodology_id"], onDelete = ForeignKey.CASCADE
    ))
])
class RuleSet(
        @ColumnInfo(name = "methodology_id")
        var methodologyId: String,
        @ColumnInfo(name = "name")
        var name: String,
        @ColumnInfo(name = "isAny")
        var isAny: Boolean,
        @ColumnInfo(name = "sample_size")
        var sampleSize: Int = 0,
        @PrimaryKey
        var id: String = UUID.randomUUID().toString()
) : Parcelable, Serializable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readInt(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(methodologyId)
        parcel.writeString(name)
        parcel.writeByte(if (isAny) 1 else 0)
        parcel.writeInt(sampleSize)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RuleSet> {
        override fun createFromParcel(parcel: Parcel): RuleSet {
            return RuleSet(parcel)
        }

        override fun newArray(size: Int): Array<RuleSet?> {
            return arrayOfNulls(size)
        }
    }
}

@Entity(tableName = "rule_record_table",
        foreignKeys = [
            (ForeignKey(
                    entity = RuleSet::class, parentColumns = ["id"], childColumns = ["rule_set_id"], onDelete = ForeignKey.CASCADE
            )),
            (ForeignKey(
                    entity = CustomField::class, parentColumns = ["id"], childColumns = ["custom_field_id"], onDelete = ForeignKey.CASCADE
            ))
        ])
class RuleRecord(
        @ColumnInfo(name = "rule_set_id")
        var ruleSetId: String,
        @ColumnInfo(name = "custom_field_id")
        var customFieldId: String,
        @ColumnInfo(name = "factory")
        var factory: String,
        @ColumnInfo(name = "rule")
        var ruleName: String,
        @ColumnInfo(name = "value")
        var value: String,
        @PrimaryKey
        var id: String = UUID.randomUUID().toString()
) : Parcelable, Serializable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ruleSetId)
        parcel.writeString(customFieldId)
        parcel.writeString(factory)
        parcel.writeString(ruleName)
        parcel.writeString(value)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RuleRecord> {
        override fun createFromParcel(parcel: Parcel): RuleRecord {
            return RuleRecord(parcel)
        }

        override fun newArray(size: Int): Array<RuleRecord?> {
            return arrayOfNulls(size)
        }
    }
}

class ResolvedRuleRecord(
        @ColumnInfo(name = "value")
        var value: String,
        @ColumnInfo(name = "factory")
        var factory: String,
        @ColumnInfo(name = "rule")
        var ruleName: String,
        @ColumnInfo(name = "custom_field_id")
        var customFieldId: String,
        var id: String) {
    @Relation(parentColumn = "custom_field_id", entityColumn = "id")
    lateinit var customFields: List<CustomField>

    val customField: CustomField
        get() = customFields.first()

    enum class Factories(val factoryName: String) {
        //banging these because anonymous classes and lambdas have no name, all our factories do though
        INT_FACTORY(IntRuleFactory::class.qualifiedName!!),
        DOUBLE_FACTORY(DoubleRuleFactory::class.qualifiedName!!),
        DATE_FACTORY(DateRuleFactory::class.qualifiedName!!),
        BOOLEAN_FACTORY(BooleanRuleFactory::class.qualifiedName!!),
        TEXT_FACTORY(TextRuleFactory::class.qualifiedName!!),
        DROPDOWN_FACTORY(DropdownRuleFactory::class.qualifiedName!!);

        companion object {
            fun factoryFor(field: CustomFieldForRules): Factories {
                return when (field) {
                    is CustomFieldForRules.DateField -> DATE_FACTORY
                    is CustomFieldForRules.DropdownField -> DROPDOWN_FACTORY
                    is CustomFieldForRules.TextField -> TEXT_FACTORY
                    is CustomFieldForRules.IntegerField -> INT_FACTORY
                    is CustomFieldForRules.DoubleField -> DOUBLE_FACTORY
                    is CustomFieldForRules.BooleanField -> BOOLEAN_FACTORY
                }
            }
        }
    }

    val rule: Rule
        get() {
            when (factory) {
                Factories.INT_FACTORY.factoryName -> {
                    val storedEnum = IntRuleFactory.Rules.valueOf(ruleName)
                    val comparisonValue = value.toInt()
                    return IntRuleFactory.makeRule(storedEnum, customField, comparisonValue)
                }
                Factories.DOUBLE_FACTORY.factoryName -> {
                    val storedEnum = DoubleRuleFactory.Rules.valueOf(ruleName)
                    val comparisonValue = value.toDouble()
                    return DoubleRuleFactory.makeRule(storedEnum, customField, comparisonValue)
                }
                Factories.DATE_FACTORY.factoryName -> {
                    val storedEnum = DateRuleFactory.Rules.valueOf(ruleName)
                    val comparisonValue = Date(value.toLong())
                    return DateRuleFactory.makeRule(storedEnum, customField, comparisonValue)
                }
                Factories.BOOLEAN_FACTORY.factoryName -> {
                    val storedEnum = BooleanRuleFactory.Rules.valueOf(ruleName)
                    val comparisonValue = value.toBoolean()
                    return BooleanRuleFactory.makeRule(storedEnum, customField, comparisonValue)

                }
                Factories.TEXT_FACTORY.factoryName -> {
                    val storedEnum = TextRuleFactory.Rules.valueOf(ruleName)
                    return TextRuleFactory.makeRule(storedEnum, customField, value)
                }
                Factories.DROPDOWN_FACTORY.factoryName -> {
                    val storedEnum = DropdownRuleFactory.Rules.valueOf(ruleName)
                    return DropdownRuleFactory.makeRule(storedEnum, customField, value)
                }
                else -> throw IllegalStateException("No factory exists with the qualified name: $factory")
            }
        }
}


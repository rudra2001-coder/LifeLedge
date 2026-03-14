package com.rudra.lifeledge.data.local.converters

import androidx.room.TypeConverter
import com.rudra.lifeledge.data.local.entity.*

class Converters {
    @TypeConverter
    fun fromDayType(value: DayType) = value.name

    @TypeConverter
    fun toDayType(value: String) = DayType.valueOf(value)

    @TypeConverter
    fun fromAccountType(value: AccountType) = value.name

    @TypeConverter
    fun toAccountType(value: String) = AccountType.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType) = value.name

    @TypeConverter
    fun toTransactionType(value: String) = TransactionType.valueOf(value)

    @TypeConverter
    fun fromFrequency(value: Frequency) = value.name

    @TypeConverter
    fun toFrequency(value: String) = Frequency.valueOf(value)

    @TypeConverter
    fun fromLoanType(value: LoanType) = value.name

    @TypeConverter
    fun toLoanType(value: String) = LoanType.valueOf(value)

    @TypeConverter
    fun fromHabitCategory(value: HabitCategory) = value.name

    @TypeConverter
    fun toHabitCategory(value: String) = HabitCategory.valueOf(value)

    @TypeConverter
    fun fromHabitFrequency(value: HabitFrequency) = value.name

    @TypeConverter
    fun toHabitFrequency(value: String) = HabitFrequency.valueOf(value)

    @TypeConverter
    fun fromMood(value: Mood) = value.name

    @TypeConverter
    fun toMood(value: String) = Mood.valueOf(value)

    @TypeConverter
    fun fromGoalType(value: GoalType) = value.name

    @TypeConverter
    fun toGoalType(value: String) = GoalType.valueOf(value)

    @TypeConverter
    fun fromAdviceType(value: AdviceType) = value.name

    @TypeConverter
    fun toAdviceType(value: String) = AdviceType.valueOf(value)

    @TypeConverter
    fun fromBackupType(value: BackupType) = value.name

    @TypeConverter
    fun toBackupType(value: String) = BackupType.valueOf(value)

    @TypeConverter
    fun fromBackupStatus(value: BackupStatus) = value.name

    @TypeConverter
    fun toBackupStatus(value: String) = BackupStatus.valueOf(value)

    @TypeConverter
    fun fromSettingType(value: SettingType) = value.name

    @TypeConverter
    fun toSettingType(value: String) = SettingType.valueOf(value)
}

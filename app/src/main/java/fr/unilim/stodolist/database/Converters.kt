package fr.unilim.stodolist.database

import androidx.room.TypeConverter
import fr.unilim.stodolist.models.TaskStatus
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun taskStatusToString(status: TaskStatus): String {
        return status.name
    }

    @TypeConverter
    fun stringToTaskStatus(value: String): TaskStatus {
        return TaskStatus.valueOf(value)
    }
}
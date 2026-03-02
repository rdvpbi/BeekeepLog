package com.beekeeplog.app.data.room.converter

import androidx.room.TypeConverter
import com.beekeeplog.app.domain.model.CloseReason
import com.beekeeplog.app.domain.model.EventType
import com.beekeeplog.app.domain.model.Genetics
import com.beekeeplog.app.domain.model.IntentType
import com.beekeeplog.app.domain.model.LifecycleStatus
import com.beekeeplog.app.domain.model.ProcessStatus
import com.beekeeplog.app.domain.model.SessionStatus
import com.beekeeplog.app.domain.model.Stage
import com.beekeeplog.app.domain.model.TaskType

/** Room type converters — all enum values are stored as their [name] strings. */
class Converters {

    @TypeConverter fun geneticsToString(v: Genetics): String = v.name
    @TypeConverter fun stringToGenetics(v: String): Genetics = Genetics.valueOf(v)

    @TypeConverter fun stageToString(v: Stage): String = v.name
    @TypeConverter fun stringToStage(v: String): Stage = Stage.valueOf(v)

    @TypeConverter fun lifecycleStatusToString(v: LifecycleStatus): String = v.name
    @TypeConverter fun stringToLifecycleStatus(v: String): LifecycleStatus = LifecycleStatus.valueOf(v)

    @TypeConverter fun taskTypeToString(v: TaskType): String = v.name
    @TypeConverter fun stringToTaskType(v: String): TaskType = TaskType.valueOf(v)

    @TypeConverter fun closeReasonToString(v: CloseReason): String = v.name
    @TypeConverter fun stringToCloseReason(v: String): CloseReason = CloseReason.valueOf(v)

    @TypeConverter fun processStatusToString(v: ProcessStatus): String = v.name
    @TypeConverter fun stringToProcessStatus(v: String): ProcessStatus = ProcessStatus.valueOf(v)

    @TypeConverter fun sessionStatusToString(v: SessionStatus): String = v.name
    @TypeConverter fun stringToSessionStatus(v: String): SessionStatus = SessionStatus.valueOf(v)

    @TypeConverter fun eventTypeToString(v: EventType): String = v.name
    @TypeConverter fun stringToEventType(v: String): EventType = EventType.valueOf(v)

    @TypeConverter fun intentTypeToString(v: IntentType): String = v.name
    @TypeConverter fun stringToIntentType(v: String): IntentType = IntentType.valueOf(v)
}

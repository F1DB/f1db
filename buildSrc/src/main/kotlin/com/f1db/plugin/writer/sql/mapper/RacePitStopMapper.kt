package com.f1db.plugin.writer.sql.mapper

import com.f1db.plugin.schema.RaceDataType
import com.f1db.plugin.schema.splitted.PitStop
import com.f1db.plugin.writer.sql.tables.records.RaceDataRecord
import org.jooq.RecordUnmapper
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy

/**
 * The pit stop mapper.
 *
 * @author Marcel Overdijk
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
interface RacePitStopMapper : RecordUnmapper<PitStop, RaceDataRecord> {

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "practiceTime", ignore = true)
    @Mapping(target = "practiceTimeMillis", ignore = true)
    @Mapping(target = "practiceGap", ignore = true)
    @Mapping(target = "practiceGapMillis", ignore = true)
    @Mapping(target = "practiceInterval", ignore = true)
    @Mapping(target = "practiceIntervalMillis", ignore = true)
    @Mapping(target = "practiceLaps", ignore = true)
    @Mapping(target = "qualifyingTime", ignore = true)
    @Mapping(target = "qualifyingTimeMillis", ignore = true)
    @Mapping(target = "qualifyingQ1", ignore = true)
    @Mapping(target = "qualifyingQ1Millis", ignore = true)
    @Mapping(target = "qualifyingQ2", ignore = true)
    @Mapping(target = "qualifyingQ2Millis", ignore = true)
    @Mapping(target = "qualifyingQ3", ignore = true)
    @Mapping(target = "qualifyingQ3Millis", ignore = true)
    @Mapping(target = "qualifyingGap", ignore = true)
    @Mapping(target = "qualifyingGapMillis", ignore = true)
    @Mapping(target = "qualifyingInterval", ignore = true)
    @Mapping(target = "qualifyingIntervalMillis", ignore = true)
    @Mapping(target = "qualifyingLaps", ignore = true)
    @Mapping(target = "startingGridPositionQualificationPositionNumber", ignore = true)
    @Mapping(target = "startingGridPositionQualificationPositionText", ignore = true)
    @Mapping(target = "startingGridPositionGridPenalty", ignore = true)
    @Mapping(target = "startingGridPositionGridPenaltyPositions", ignore = true)
    @Mapping(target = "startingGridPositionTime", ignore = true)
    @Mapping(target = "startingGridPositionTimeMillis", ignore = true)
    @Mapping(target = "raceSharedCar", ignore = true)
    @Mapping(target = "raceLaps", ignore = true)
    @Mapping(target = "raceTime", ignore = true)
    @Mapping(target = "raceTimeMillis", ignore = true)
    @Mapping(target = "raceTimePenalty", ignore = true)
    @Mapping(target = "raceTimePenaltyMillis", ignore = true)
    @Mapping(target = "raceGap", ignore = true)
    @Mapping(target = "raceGapMillis", ignore = true)
    @Mapping(target = "raceGapLaps", ignore = true)
    @Mapping(target = "raceInterval", ignore = true)
    @Mapping(target = "raceIntervalMillis", ignore = true)
    @Mapping(target = "raceReasonRetired", ignore = true)
    @Mapping(target = "racePoints", ignore = true)
    @Mapping(target = "racePolePosition", ignore = true)
    @Mapping(target = "raceQualificationPositionNumber", ignore = true)
    @Mapping(target = "raceQualificationPositionText", ignore = true)
    @Mapping(target = "raceGridPositionNumber", ignore = true)
    @Mapping(target = "raceGridPositionText", ignore = true)
    @Mapping(target = "racePositionsGained", ignore = true)
    @Mapping(target = "racePitStops", ignore = true)
    @Mapping(target = "raceFastestLap", ignore = true)
    @Mapping(target = "raceDriverOfTheDay", ignore = true)
    @Mapping(target = "raceGrandSlam", ignore = true)
    @Mapping(target = "fastestLapLap", ignore = true)
    @Mapping(target = "fastestLapTime", ignore = true)
    @Mapping(target = "fastestLapTimeMillis", ignore = true)
    @Mapping(target = "fastestLapGap", ignore = true)
    @Mapping(target = "fastestLapGapMillis", ignore = true)
    @Mapping(target = "fastestLapInterval", ignore = true)
    @Mapping(target = "fastestLapIntervalMillis", ignore = true)
    @Mapping(target = "pitStopStop", source = "stop")
    @Mapping(target = "pitStopLap", source = "lap")
    @Mapping(target = "pitStopTime", source = "time")
    @Mapping(target = "pitStopTimeMillis", source = "timeMillis")
    @Mapping(target = "driverOfTheDayPercentage", ignore = true)
    override fun unmap(source: PitStop): RaceDataRecord

    fun unmap(source: PitStop, type: RaceDataType): RaceDataRecord {
        return unmap(source).apply { setType(type.name) }
    }

    fun unmap(sources: List<PitStop>, type: RaceDataType): List<RaceDataRecord> {
        return sources.map { unmap(it, type) }
    }
}

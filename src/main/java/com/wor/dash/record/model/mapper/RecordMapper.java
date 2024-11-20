package com.wor.dash.record.model.mapper;

import com.wor.dash.record.model.Record;
import com.wor.dash.record.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecordMapper {
    void batchInsertRecords(List<Record> batch);

    Cadence getCadenceData(@Param("userId") int userId,
                           @Param("startTime") String startTime,
                           @Param("endTime") String endTime);

    List<Calorie> getCalorieList(@Param("userId") int userId,
                                 @Param("startTime") String startTime,
                                 @Param("endTime") String endTime);

    Distance getDistanceData(@Param("userId") int userId,
                             @Param("startTime") String startTime,
                             @Param("endTime") String endTime);

    List<HeartRate> getHeartRateList(@Param("userId") int userId,
                                     @Param("startTime") String startTime,
                                     @Param("endTime") String endTime);

    List<Record> getRecordList(@Param("userId") int userId,
                               @Param("startTime") String startTime,
                               @Param("endTime") String endTime);

    Speed getSpeedData(@Param("userId") int userId,
                       @Param("startTime") String startTime,
                       @Param("endTime") String endTime);

    List<Step> getStepList(@Param("userId") int userId,
                           @Param("startTime") String startTime,
                           @Param("endTime") String endTime);

    List<ExerciseTime> getExerciseTimeList(@Param("userId") int userId,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime);
} 

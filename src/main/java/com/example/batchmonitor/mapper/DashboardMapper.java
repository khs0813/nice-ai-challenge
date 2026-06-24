package com.example.batchmonitor.mapper;

import com.example.batchmonitor.dto.JobExecutionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DashboardMapper {

    long countTotalJobs();

    long countTodayJobs(@Param("startDateTime") LocalDateTime startDateTime,
                        @Param("endDateTime") LocalDateTime endDateTime);

    long countCompletedJobs();

    long countFailedJobs();

    long countRunningJobs();

    long countRecentFailedJobs(@Param("fromDateTime") LocalDateTime fromDateTime);

    String findLatestJobName();

    List<JobExecutionDto> findRecentJobExecutions(@Param("limit") int limit);

    List<JobExecutionDto> findCompletedExecutionsForAverage(@Param("limit") int limit);

    List<JobExecutionDto> findTotalJobExecutions(@Param("offset") int offset,
                                                 @Param("size") int size);

    List<JobExecutionDto> findTodayJobExecutions(@Param("startDateTime") LocalDateTime startDateTime,
                                                 @Param("endDateTime") LocalDateTime endDateTime,
                                                 @Param("offset") int offset,
                                                 @Param("size") int size);

    List<JobExecutionDto> findCompletedJobExecutions(@Param("offset") int offset,
                                                     @Param("size") int size);

    List<JobExecutionDto> findFailedJobExecutions(@Param("offset") int offset,
                                                  @Param("size") int size);

    List<JobExecutionDto> findRunningJobExecutions(@Param("offset") int offset,
                                                   @Param("size") int size);

    List<JobExecutionDto> findRecentFailedJobExecutions(@Param("fromDateTime") LocalDateTime fromDateTime,
                                                        @Param("offset") int offset,
                                                        @Param("size") int size);
}

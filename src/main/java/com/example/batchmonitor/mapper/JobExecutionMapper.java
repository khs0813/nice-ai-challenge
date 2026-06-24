package com.example.batchmonitor.mapper;

import com.example.batchmonitor.dto.JobExecutionDetailDto;
import com.example.batchmonitor.dto.JobExecutionDto;
import com.example.batchmonitor.dto.SearchConditionDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobExecutionMapper {

    List<JobExecutionDto> findJobExecutions(SearchConditionDto condition);

    long countJobExecutions(SearchConditionDto condition);

    List<JobExecutionDto> findFailedJobExecutions(SearchConditionDto condition);

    long countFailedJobExecutions(SearchConditionDto condition);

    List<JobExecutionDto> findRunningJobExecutions(SearchConditionDto condition);

    long countRunningJobExecutions(SearchConditionDto condition);

    JobExecutionDetailDto findJobExecutionDetail(Long jobExecutionId);
}

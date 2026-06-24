package com.example.batchmonitor.mapper;

import com.example.batchmonitor.dto.JobParameterDto;
import com.example.batchmonitor.dto.SearchConditionDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobParameterMapper {

    List<JobParameterDto> findJobParametersLegacy(SearchConditionDto condition);

    long countJobParametersLegacy(SearchConditionDto condition);

    List<JobParameterDto> findJobParametersModern(SearchConditionDto condition);

    long countJobParametersModern(SearchConditionDto condition);

    List<JobParameterDto> findJobParametersByJobExecutionIdLegacy(Long jobExecutionId);

    List<JobParameterDto> findJobParametersByJobExecutionIdModern(Long jobExecutionId);
}

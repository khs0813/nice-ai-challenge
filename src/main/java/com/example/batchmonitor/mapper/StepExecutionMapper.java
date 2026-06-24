package com.example.batchmonitor.mapper;

import com.example.batchmonitor.dto.StepExecutionDetailDto;
import com.example.batchmonitor.dto.StepExecutionDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StepExecutionMapper {

    List<StepExecutionDto> findStepsByJobExecutionId(Long jobExecutionId);

    StepExecutionDetailDto findStepExecutionDetail(Long stepExecutionId);
}

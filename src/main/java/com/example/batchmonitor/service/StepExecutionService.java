package com.example.batchmonitor.service;

import com.example.batchmonitor.dto.StepExecutionDetailDto;
import com.example.batchmonitor.mapper.StepExecutionMapper;
import com.example.batchmonitor.util.DateTimeUtils;
import org.springframework.stereotype.Service;

@Service
public class StepExecutionService {

    private final StepExecutionMapper stepExecutionMapper;

    public StepExecutionService(StepExecutionMapper stepExecutionMapper) {
        this.stepExecutionMapper = stepExecutionMapper;
    }

    public StepExecutionDetailDto findStepExecutionDetail(Long stepExecutionId) {
        StepExecutionDetailDto detail = stepExecutionMapper.findStepExecutionDetail(stepExecutionId);
        if (detail == null) {
            throw new IllegalArgumentException("세부 단계 실행 정보를 찾을 수 없습니다. 단계 실행 번호=" + stepExecutionId);
        }
        detail.setDurationText(DateTimeUtils.durationText(detail.getStartTime(), detail.getEndTime()));
        return detail;
    }
}

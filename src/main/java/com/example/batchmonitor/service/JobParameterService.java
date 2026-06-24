package com.example.batchmonitor.service;

import com.example.batchmonitor.dto.JobParameterDto;
import com.example.batchmonitor.dto.PageResult;
import com.example.batchmonitor.dto.SearchConditionDto;
import com.example.batchmonitor.mapper.JobParameterMapper;
import com.example.batchmonitor.util.DateTimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobParameterService {

    private final JobParameterMapper jobParameterMapper;

    @Value("${batch.monitor.parameter-schema:legacy}")
    private String parameterSchema;

    public JobParameterService(JobParameterMapper jobParameterMapper) {
        this.jobParameterMapper = jobParameterMapper;
    }

    public PageResult<JobParameterDto> findJobParameters(SearchConditionDto condition) {
        prepare(condition);
        List<JobParameterDto> parameters;
        long totalCount;
        if (isModernParameterSchema()) {
            parameters = jobParameterMapper.findJobParametersModern(condition);
            totalCount = jobParameterMapper.countJobParametersModern(condition);
        } else {
            parameters = jobParameterMapper.findJobParametersLegacy(condition);
            totalCount = jobParameterMapper.countJobParametersLegacy(condition);
        }
        return new PageResult<JobParameterDto>(parameters, totalCount, condition.getPage(), condition.getSize());
    }

    public List<JobParameterDto> findJobParametersByJobExecutionId(Long jobExecutionId) {
        if (isModernParameterSchema()) {
            return jobParameterMapper.findJobParametersByJobExecutionIdModern(jobExecutionId);
        }
        return jobParameterMapper.findJobParametersByJobExecutionIdLegacy(jobExecutionId);
    }

    private void prepare(SearchConditionDto condition) {
        condition.normalizePaging();
        condition.setStartDateTime(DateTimeUtils.parseStartDate(condition.getStartDate()));
        condition.setEndDateTime(DateTimeUtils.parseEndDateExclusive(condition.getEndDate()));
    }

    private boolean isModernParameterSchema() {
        return "modern".equalsIgnoreCase(parameterSchema) || "new".equalsIgnoreCase(parameterSchema);
    }
}

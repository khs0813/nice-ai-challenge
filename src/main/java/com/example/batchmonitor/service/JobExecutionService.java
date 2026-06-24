package com.example.batchmonitor.service;

import com.example.batchmonitor.dto.JobExecutionDetailDto;
import com.example.batchmonitor.dto.JobExecutionDto;
import com.example.batchmonitor.dto.PageResult;
import com.example.batchmonitor.dto.SearchConditionDto;
import com.example.batchmonitor.dto.StepExecutionDto;
import com.example.batchmonitor.mapper.JobExecutionMapper;
import com.example.batchmonitor.mapper.StepExecutionMapper;
import com.example.batchmonitor.util.DateTimeUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobExecutionService {

    private final JobExecutionMapper jobExecutionMapper;
    private final StepExecutionMapper stepExecutionMapper;
    private final JobParameterService jobParameterService;

    public JobExecutionService(JobExecutionMapper jobExecutionMapper,
                               StepExecutionMapper stepExecutionMapper,
                               JobParameterService jobParameterService) {
        this.jobExecutionMapper = jobExecutionMapper;
        this.stepExecutionMapper = stepExecutionMapper;
        this.jobParameterService = jobParameterService;
    }

    public PageResult<JobExecutionDto> findJobExecutions(SearchConditionDto condition) {
        prepare(condition);
        List<JobExecutionDto> jobs = jobExecutionMapper.findJobExecutions(condition);
        enrichJobs(jobs);
        long totalCount = jobExecutionMapper.countJobExecutions(condition);
        return new PageResult<JobExecutionDto>(jobs, totalCount, condition.getPage(), condition.getSize());
    }

    public PageResult<JobExecutionDto> findFailedJobExecutions(SearchConditionDto condition) {
        prepare(condition);
        List<JobExecutionDto> jobs = jobExecutionMapper.findFailedJobExecutions(condition);
        enrichJobs(jobs);
        long totalCount = jobExecutionMapper.countFailedJobExecutions(condition);
        return new PageResult<JobExecutionDto>(jobs, totalCount, condition.getPage(), condition.getSize());
    }

    public PageResult<JobExecutionDto> findRunningJobExecutions(SearchConditionDto condition) {
        prepare(condition);
        List<JobExecutionDto> jobs = jobExecutionMapper.findRunningJobExecutions(condition);
        enrichJobs(jobs);
        long totalCount = jobExecutionMapper.countRunningJobExecutions(condition);
        return new PageResult<JobExecutionDto>(jobs, totalCount, condition.getPage(), condition.getSize());
    }

    public JobExecutionDetailDto findJobExecutionDetail(Long jobExecutionId) {
        JobExecutionDetailDto detail = jobExecutionMapper.findJobExecutionDetail(jobExecutionId);
        if (detail == null) {
            throw new IllegalArgumentException("배치 실행 정보를 찾을 수 없습니다. 실행 번호=" + jobExecutionId);
        }
        detail.setDurationText(DateTimeUtils.durationText(detail.getStartTime(), detail.getEndTime()));
        detail.setParameters(jobParameterService.findJobParametersByJobExecutionId(jobExecutionId));

        List<StepExecutionDto> steps = stepExecutionMapper.findStepsByJobExecutionId(jobExecutionId);
        enrichSteps(steps);
        detail.setSteps(steps);
        return detail;
    }

    private void prepare(SearchConditionDto condition) {
        condition.normalizePaging();
        condition.setStartDateTime(DateTimeUtils.parseStartDate(condition.getStartDate()));
        condition.setEndDateTime(DateTimeUtils.parseEndDateExclusive(condition.getEndDate()));
    }

    private void enrichJobs(List<JobExecutionDto> jobs) {
        if (jobs == null) {
            return;
        }
        for (JobExecutionDto job : jobs) {
            job.setDurationText(DateTimeUtils.durationText(job.getStartTime(), job.getEndTime()));
        }
    }

    private void enrichSteps(List<StepExecutionDto> steps) {
        if (steps == null) {
            return;
        }
        for (StepExecutionDto step : steps) {
            step.setDurationText(DateTimeUtils.durationText(step.getStartTime(), step.getEndTime()));
        }
    }
}

package com.example.batchmonitor.dto;

import java.util.ArrayList;
import java.util.List;

public class JobExecutionDetailDto extends JobExecutionDto {

    private List<JobParameterDto> parameters = new ArrayList<JobParameterDto>();
    private List<StepExecutionDto> steps = new ArrayList<StepExecutionDto>();

    public List<JobParameterDto> getParameters() {
        return parameters;
    }

    public void setParameters(List<JobParameterDto> parameters) {
        this.parameters = parameters;
    }

    public List<StepExecutionDto> getSteps() {
        return steps;
    }

    public void setSteps(List<StepExecutionDto> steps) {
        this.steps = steps;
    }
}

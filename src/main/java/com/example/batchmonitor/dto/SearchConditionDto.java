package com.example.batchmonitor.dto;

import java.time.LocalDateTime;

public class SearchConditionDto {

    private Long jobExecutionId;
    private String jobName;
    private String status;
    private String exitCode;
    private String startDate;
    private String endDate;
    private String parameterName;
    private String parameterValue;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int page = 1;
    private int size = 20;
    private int offset;

    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = trimToNull(jobName);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = trimToNull(status);
    }

    public String getExitCode() {
        return exitCode;
    }

    public void setExitCode(String exitCode) {
        this.exitCode = trimToNull(exitCode);
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = trimToNull(startDate);
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = trimToNull(endDate);
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = trimToNull(parameterName);
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = trimToNull(parameterValue);
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void normalizePaging() {
        if (page < 1) {
            page = 1;
        }
        if (page > 100000) {
            page = 100000;
        }
        if (size < 1) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }
        long calculatedOffset = (long) (page - 1) * (long) size;
        offset = calculatedOffset > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) calculatedOffset;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

package com.example.batchmonitor.dto;

import com.example.batchmonitor.util.DateTimeUtils;

import java.time.LocalDateTime;

public class StepExecutionDto {

    private Long stepExecutionId;
    private Long jobExecutionId;
    private Integer version;
    private String stepName;
    private String jobName;
    private String status;
    private String exitCode;
    private String exitMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastUpdated;
    private Long commitCount;
    private Long readCount;
    private Long filterCount;
    private Long writeCount;
    private Long readSkipCount;
    private Long writeSkipCount;
    private Long processSkipCount;
    private Long rollbackCount;
    private String durationText;

    public Long getStepExecutionId() {
        return stepExecutionId;
    }

    public void setStepExecutionId(Long stepExecutionId) {
        this.stepExecutionId = stepExecutionId;
    }

    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExitCode() {
        return exitCode;
    }

    public void setExitCode(String exitCode) {
        this.exitCode = exitCode;
    }

    public String getExitMessage() {
        return exitMessage;
    }

    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(Long commitCount) {
        this.commitCount = commitCount;
    }

    public Long getReadCount() {
        return readCount;
    }

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
    }

    public Long getFilterCount() {
        return filterCount;
    }

    public void setFilterCount(Long filterCount) {
        this.filterCount = filterCount;
    }

    public Long getWriteCount() {
        return writeCount;
    }

    public void setWriteCount(Long writeCount) {
        this.writeCount = writeCount;
    }

    public Long getReadSkipCount() {
        return readSkipCount;
    }

    public void setReadSkipCount(Long readSkipCount) {
        this.readSkipCount = readSkipCount;
    }

    public Long getWriteSkipCount() {
        return writeSkipCount;
    }

    public void setWriteSkipCount(Long writeSkipCount) {
        this.writeSkipCount = writeSkipCount;
    }

    public Long getProcessSkipCount() {
        return processSkipCount;
    }

    public void setProcessSkipCount(Long processSkipCount) {
        this.processSkipCount = processSkipCount;
    }

    public Long getRollbackCount() {
        return rollbackCount;
    }

    public void setRollbackCount(Long rollbackCount) {
        this.rollbackCount = rollbackCount;
    }

    public String getDurationText() {
        return durationText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public String getStartTimeText() {
        return DateTimeUtils.format(startTime);
    }

    public String getEndTimeText() {
        return DateTimeUtils.format(endTime);
    }

    public String getLastUpdatedText() {
        return DateTimeUtils.format(lastUpdated);
    }

    public String getExitMessageSummary() {
        return DateTimeUtils.summarize(exitMessage, 100);
    }

    public Long getTotalSkipCount() {
        return safe(readSkipCount) + safe(writeSkipCount) + safe(processSkipCount);
    }

    public String getStatusLabel() {
        if (status == null) {
            return "-";
        }
        String normalized = status.toUpperCase();
        if ("COMPLETED".equals(normalized)) {
            return "성공";
        }
        if ("FAILED".equals(normalized)) {
            return "실패";
        }
        if ("STARTING".equals(normalized) || "STARTED".equals(normalized)) {
            return "실행 중";
        }
        if ("STOPPING".equals(normalized)) {
            return "중지 중";
        }
        if ("STOPPED".equals(normalized)) {
            return "중지됨";
        }
        if ("ABANDONED".equals(normalized)) {
            return "중단됨";
        }
        return status;
    }

    public String getExitCodeLabel() {
        if (exitCode == null) {
            return "-";
        }
        String normalized = exitCode.toUpperCase();
        if ("COMPLETED".equals(normalized)) {
            return "정상 종료";
        }
        if ("FAILED".equals(normalized)) {
            return "실패 종료";
        }
        if ("STOPPED".equals(normalized)) {
            return "중지됨";
        }
        return exitCode;
    }

    public String getStatusBadgeClass() {
        if (status == null) {
            return "badge-unknown";
        }
        String normalized = status.toUpperCase();
        if ("COMPLETED".equals(normalized)) {
            return "badge-completed";
        }
        if ("FAILED".equals(normalized)) {
            return "badge-failed";
        }
        if ("STARTING".equals(normalized) || "STARTED".equals(normalized)) {
            return "badge-started";
        }
        if ("STOPPING".equals(normalized) || "STOPPED".equals(normalized) || "ABANDONED".equals(normalized)) {
            return "badge-stopped";
        }
        return "badge-unknown";
    }

    private long safe(Long value) {
        return value == null ? 0L : value;
    }
}

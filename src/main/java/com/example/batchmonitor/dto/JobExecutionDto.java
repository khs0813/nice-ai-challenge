package com.example.batchmonitor.dto;

import com.example.batchmonitor.util.DateTimeUtils;

import java.time.LocalDateTime;

public class JobExecutionDto {

    private Long jobExecutionId;
    private Long jobInstanceId;
    private Integer version;
    private String jobName;
    private String status;
    private String exitCode;
    private String exitMessage;
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastUpdated;
    private String durationText;

    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
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

    public String getDurationText() {
        return durationText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public String getCreateTimeText() {
        return DateTimeUtils.format(createTime);
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
}

package com.example.batchmonitor.dto;

import com.example.batchmonitor.util.DateTimeUtils;

import java.time.LocalDateTime;

public class ValidationResultDto {

    private String validationType;
    private Long resultId;
    private Long ruleId;
    private String ruleName;
    private String resultStatus;
    private String requestedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long leftRowCount;
    private Long rightRowCount;
    private String summary;

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getLeftRowCount() {
        return leftRowCount;
    }

    public void setLeftRowCount(Long leftRowCount) {
        this.leftRowCount = leftRowCount;
    }

    public Long getRightRowCount() {
        return rightRowCount;
    }

    public void setRightRowCount(Long rightRowCount) {
        this.rightRowCount = rightRowCount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRequestedAtText() {
        return DateTimeUtils.format(requestedAt);
    }

    public String getDurationText() {
        return DateTimeUtils.durationText(startedAt, finishedAt);
    }

    public String getSummaryText() {
        return DateTimeUtils.summarize(summary, 120);
    }

    public String getValidationTypeLabel() {
        return "SINGLE".equalsIgnoreCase(validationType) ? "기대값 검증" : "DB 간 비교";
    }

    public String getResultStatusLabel() {
        if (resultStatus == null) {
            return "-";
        }
        String normalized = resultStatus.toUpperCase();
        if ("SUCCESS".equals(normalized)) {
            return "성공";
        }
        if ("FAIL".equals(normalized)) {
            return "불일치";
        }
        if ("ERROR".equals(normalized)) {
            return "오류";
        }
        if ("REQUESTED".equals(normalized)) {
            return "대기";
        }
        if ("RUNNING".equals(normalized)) {
            return "실행 중";
        }
        return resultStatus;
    }

    public String getStatusBadgeClass() {
        if (resultStatus == null) {
            return "badge-unknown";
        }
        String normalized = resultStatus.toUpperCase();
        if ("SUCCESS".equals(normalized)) {
            return "badge-completed";
        }
        if ("FAIL".equals(normalized) || "ERROR".equals(normalized)) {
            return "badge-failed";
        }
        if ("REQUESTED".equals(normalized) || "RUNNING".equals(normalized)) {
            return "badge-started";
        }
        return "badge-unknown";
    }
}

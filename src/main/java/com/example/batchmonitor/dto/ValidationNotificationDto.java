package com.example.batchmonitor.dto;

import com.example.batchmonitor.util.DateTimeUtils;

import java.time.LocalDateTime;

public class ValidationNotificationDto {

    private Long notificationId;
    private String validationType;
    private Long ruleId;
    private Long resultId;
    private String resultStatus;
    private String title;
    private String message;
    private String recipients;
    private String readYn;
    private LocalDateTime createdAt;

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getReadYn() {
        return readYn;
    }

    public void setReadYn(String readYn) {
        this.readYn = readYn;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedAtText() {
        return DateTimeUtils.format(createdAt);
    }

    public String getMessageSummary() {
        return DateTimeUtils.summarize(message, 140);
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
        return resultStatus;
    }

    public String getReadLabel() {
        return "Y".equalsIgnoreCase(readYn) ? "읽음" : "미읽음";
    }

    public String getStatusBadgeClass() {
        if ("SUCCESS".equalsIgnoreCase(resultStatus)) {
            return "badge-completed";
        }
        if ("FAIL".equalsIgnoreCase(resultStatus) || "ERROR".equalsIgnoreCase(resultStatus)) {
            return "badge-failed";
        }
        return "badge-unknown";
    }
}

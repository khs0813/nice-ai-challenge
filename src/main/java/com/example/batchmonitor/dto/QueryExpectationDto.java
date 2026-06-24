package com.example.batchmonitor.dto;

import com.example.batchmonitor.util.DateTimeUtils;

import java.time.LocalDateTime;

public class QueryExpectationDto {

    private Long expectationId;
    private String expectationName;
    private String description;
    private String targetDbType = "ORACLE";
    private String queryText;
    private String expectedValueType = "SCALAR";
    private String expectedOperator = "EQ";
    private String expectedValue;
    private String enabledYn = "Y";
    private String scheduleEnabledYn = "N";
    private String cronExpression;
    private String notifyEnabledYn = "Y";
    private String notifyOnSuccessYn = "N";
    private String notifyRecipients;
    private String aiSqlReviewYn = "N";
    private String aiSqlReviewStatus;
    private String aiSqlReviewSummary;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public Long getExpectationId() {
        return expectationId;
    }

    public void setExpectationId(Long expectationId) {
        this.expectationId = expectationId;
    }

    public String getExpectationName() {
        return expectationName;
    }

    public void setExpectationName(String expectationName) {
        this.expectationName = trimToNull(expectationName);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trimToNull(description);
    }

    public String getTargetDbType() {
        return targetDbType;
    }

    public void setTargetDbType(String targetDbType) {
        this.targetDbType = upperOrDefault(targetDbType, "ORACLE");
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = trimToNull(queryText);
    }

    public String getExpectedValueType() {
        return expectedValueType;
    }

    public void setExpectedValueType(String expectedValueType) {
        this.expectedValueType = upperOrDefault(expectedValueType, "SCALAR");
    }

    public String getExpectedOperator() {
        return expectedOperator;
    }

    public void setExpectedOperator(String expectedOperator) {
        this.expectedOperator = upperOrDefault(expectedOperator, "EQ");
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = trimToNull(expectedValue);
    }

    public String getEnabledYn() {
        return enabledYn;
    }

    public void setEnabledYn(String enabledYn) {
        this.enabledYn = "N".equalsIgnoreCase(enabledYn) ? "N" : "Y";
    }

    public String getScheduleEnabledYn() {
        return scheduleEnabledYn;
    }

    public void setScheduleEnabledYn(String scheduleEnabledYn) {
        this.scheduleEnabledYn = "Y".equalsIgnoreCase(scheduleEnabledYn) ? "Y" : "N";
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = trimToNull(cronExpression);
    }

    public String getNotifyEnabledYn() {
        return notifyEnabledYn;
    }

    public void setNotifyEnabledYn(String notifyEnabledYn) {
        this.notifyEnabledYn = "N".equalsIgnoreCase(notifyEnabledYn) ? "N" : "Y";
    }

    public String getNotifyOnSuccessYn() {
        return notifyOnSuccessYn;
    }

    public void setNotifyOnSuccessYn(String notifyOnSuccessYn) {
        this.notifyOnSuccessYn = "Y".equalsIgnoreCase(notifyOnSuccessYn) ? "Y" : "N";
    }

    public String getNotifyRecipients() {
        return notifyRecipients;
    }

    public void setNotifyRecipients(String notifyRecipients) {
        this.notifyRecipients = trimToNull(notifyRecipients);
    }

    public String getAiSqlReviewYn() {
        return aiSqlReviewYn;
    }

    public void setAiSqlReviewYn(String aiSqlReviewYn) {
        this.aiSqlReviewYn = "Y".equalsIgnoreCase(aiSqlReviewYn) ? "Y" : "N";
    }

    public String getAiSqlReviewStatus() {
        return aiSqlReviewStatus;
    }

    public void setAiSqlReviewStatus(String aiSqlReviewStatus) {
        this.aiSqlReviewStatus = trimToNull(aiSqlReviewStatus);
    }

    public String getAiSqlReviewSummary() {
        return aiSqlReviewSummary;
    }

    public void setAiSqlReviewSummary(String aiSqlReviewSummary) {
        this.aiSqlReviewSummary = trimToNull(aiSqlReviewSummary);
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = trimToNull(createdBy);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = trimToNull(updatedBy);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAtText() {
        return DateTimeUtils.format(createdAt);
    }

    public String getUpdatedAtText() {
        return DateTimeUtils.format(updatedAt);
    }

    public String getQuerySummary() {
        return DateTimeUtils.summarize(queryText, 90);
    }

    public String getExpectedValueSummary() {
        return DateTimeUtils.summarize(expectedValue, 90);
    }

    public String getExpectedValueTypeLabel() {
        return valueTypeLabel(expectedValueType);
    }

    public String getExpectedOperatorLabel() {
        return operatorLabel(expectedOperator);
    }

    public String getEnabledLabel() {
        return "Y".equalsIgnoreCase(enabledYn) ? "사용" : "중지";
    }

    public String getScheduleEnabledLabel() {
        return "Y".equalsIgnoreCase(scheduleEnabledYn) ? "사용" : "중지";
    }

    public String getNotifyEnabledLabel() {
        return "Y".equalsIgnoreCase(notifyEnabledYn) ? "사용" : "중지";
    }

    public String getAiSqlReviewStatusLabel() {
        if (aiSqlReviewStatus == null) {
            return "검토 없음";
        }
        String normalized = aiSqlReviewStatus.toUpperCase();
        if ("PASS".equals(normalized)) {
            return "검토 통과";
        }
        if ("WARN".equals(normalized)) {
            return "주의 필요";
        }
        if ("FAIL".equals(normalized)) {
            return "저장 차단";
        }
        return aiSqlReviewStatus;
    }

    private String valueTypeLabel(String valueType) {
        if ("SCALAR".equalsIgnoreCase(valueType)) {
            return "단일 값";
        }
        if ("ROW_COUNT".equalsIgnoreCase(valueType)) {
            return "조회 건수";
        }
        if ("JSON".equalsIgnoreCase(valueType)) {
            return "전체 결과(JSON)";
        }
        if ("HASH".equalsIgnoreCase(valueType)) {
            return "전체 결과 해시";
        }
        return valueType;
    }

    private String operatorLabel(String operator) {
        if ("EQ".equalsIgnoreCase(operator)) {
            return "같음";
        }
        if ("NE".equalsIgnoreCase(operator)) {
            return "다름";
        }
        if ("GT".equalsIgnoreCase(operator)) {
            return "초과";
        }
        if ("GTE".equalsIgnoreCase(operator)) {
            return "이상";
        }
        if ("LT".equalsIgnoreCase(operator)) {
            return "미만";
        }
        if ("LTE".equalsIgnoreCase(operator)) {
            return "이하";
        }
        if ("CONTAINS".equalsIgnoreCase(operator)) {
            return "포함";
        }
        return operator;
    }

    private String upperOrDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed.toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

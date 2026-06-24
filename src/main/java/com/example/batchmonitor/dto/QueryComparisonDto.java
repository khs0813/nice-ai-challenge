package com.example.batchmonitor.dto;

import com.example.batchmonitor.util.DateTimeUtils;

import java.time.LocalDateTime;

public class QueryComparisonDto {

    private Long comparisonId;
    private String comparisonName;
    private String description;
    private String sybaseQuery;
    private String oracleQuery;
    private String compareMode = "EXACT";
    private String enabledYn = "Y";
    private String scheduleEnabledYn = "N";
    private String cronExpression;
    private String notifyEnabledYn = "Y";
    private String notifyOnSuccessYn = "N";
    private String notifyRecipients;
    private String aiSqlReviewYn = "N";
    private String aiAnalysisEnabledYn = "Y";
    private String aiSqlReviewStatus;
    private String aiSqlReviewSummary;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public Long getComparisonId() {
        return comparisonId;
    }

    public void setComparisonId(Long comparisonId) {
        this.comparisonId = comparisonId;
    }

    public String getComparisonName() {
        return comparisonName;
    }

    public void setComparisonName(String comparisonName) {
        this.comparisonName = trimToNull(comparisonName);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trimToNull(description);
    }

    public String getSybaseQuery() {
        return sybaseQuery;
    }

    public void setSybaseQuery(String sybaseQuery) {
        this.sybaseQuery = trimToNull(sybaseQuery);
    }

    public String getOracleQuery() {
        return oracleQuery;
    }

    public void setOracleQuery(String oracleQuery) {
        this.oracleQuery = trimToNull(oracleQuery);
    }

    public String getCompareMode() {
        return compareMode;
    }

    public void setCompareMode(String compareMode) {
        this.compareMode = trimToNull(compareMode);
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

    public String getAiAnalysisEnabledYn() {
        return aiAnalysisEnabledYn;
    }

    public void setAiAnalysisEnabledYn(String aiAnalysisEnabledYn) {
        this.aiAnalysisEnabledYn = "N".equalsIgnoreCase(aiAnalysisEnabledYn) ? "N" : "Y";
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

    public String getSybaseQuerySummary() {
        return DateTimeUtils.summarize(sybaseQuery, 90);
    }

    public String getOracleQuerySummary() {
        return DateTimeUtils.summarize(oracleQuery, 90);
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

    public String getAiSqlReviewEnabledLabel() {
        return "Y".equalsIgnoreCase(aiSqlReviewYn) ? "사용" : "중지";
    }

    public String getAiAnalysisEnabledLabel() {
        return "Y".equalsIgnoreCase(aiAnalysisEnabledYn) ? "사용" : "중지";
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

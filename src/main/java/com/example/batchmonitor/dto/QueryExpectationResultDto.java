package com.example.batchmonitor.dto;

import com.example.batchmonitor.util.DateTimeUtils;

import java.time.LocalDateTime;

public class QueryExpectationResultDto {

    private Long resultId;
    private Long expectationId;
    private String expectationName;
    private String targetDbType;
    private String expectedValueType;
    private String expectedOperator;
    private String requestedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String resultStatus;
    private Long actualRowCount;
    private String actualValue;
    private String actualResultHash;
    private String expectedValue;
    private String mismatchSummary;
    private String errorMessage;

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

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
        this.expectationName = expectationName;
    }

    public String getTargetDbType() {
        return targetDbType;
    }

    public void setTargetDbType(String targetDbType) {
        this.targetDbType = targetDbType;
    }

    public String getExpectedValueType() {
        return expectedValueType;
    }

    public void setExpectedValueType(String expectedValueType) {
        this.expectedValueType = expectedValueType;
    }

    public String getExpectedOperator() {
        return expectedOperator;
    }

    public void setExpectedOperator(String expectedOperator) {
        this.expectedOperator = expectedOperator;
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

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public Long getActualRowCount() {
        return actualRowCount;
    }

    public void setActualRowCount(Long actualRowCount) {
        this.actualRowCount = actualRowCount;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public String getActualResultHash() {
        return actualResultHash;
    }

    public void setActualResultHash(String actualResultHash) {
        this.actualResultHash = actualResultHash;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public String getMismatchSummary() {
        return mismatchSummary;
    }

    public void setMismatchSummary(String mismatchSummary) {
        this.mismatchSummary = mismatchSummary;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRequestedAtText() {
        return DateTimeUtils.format(requestedAt);
    }

    public String getStartedAtText() {
        return DateTimeUtils.format(startedAt);
    }

    public String getFinishedAtText() {
        return DateTimeUtils.format(finishedAt);
    }

    public String getDurationText() {
        return DateTimeUtils.durationText(startedAt, finishedAt);
    }

    public String getActualValueSummary() {
        return DateTimeUtils.summarize(actualValue, 120);
    }

    public String getExpectedValueSummary() {
        return DateTimeUtils.summarize(expectedValue, 120);
    }

    public String getExpectedValueTypeLabel() {
        return valueTypeLabel(expectedValueType);
    }

    public String getExpectedOperatorLabel() {
        return operatorLabel(expectedOperator);
    }

    public String getMessageSummary() {
        String message = mismatchSummary != null ? mismatchSummary : errorMessage;
        return DateTimeUtils.summarize(message, 120);
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
}

package com.example.batchmonitor.dto;

import java.time.LocalDateTime;

public class QueryExpectationSearchConditionDto {

    private Long expectationId;
    private String expectationName;
    private String targetDbType;
    private String enabledYn;
    private String resultStatus;
    private String requestedBy;
    private String startDate;
    private String endDate;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int page = 1;
    private int size = 20;
    private int offset;

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

    public String getTargetDbType() {
        return targetDbType;
    }

    public void setTargetDbType(String targetDbType) {
        this.targetDbType = upperToNull(targetDbType);
    }

    public String getEnabledYn() {
        return enabledYn;
    }

    public void setEnabledYn(String enabledYn) {
        this.enabledYn = trimToNull(enabledYn);
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = upperToNull(resultStatus);
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = trimToNull(requestedBy);
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

    private String upperToNull(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

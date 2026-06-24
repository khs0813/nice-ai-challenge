package com.example.batchmonitor.dto;

public class DashboardValidationTrendDto {

    private String dateLabel;
    private long successCount;
    private long failCount;
    private long errorCount;
    private long otherCount;
    private long maxCount = 1;

    public String getDateLabel() {
        return dateLabel;
    }

    public void setDateLabel(String dateLabel) {
        this.dateLabel = dateLabel;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public long getFailCount() {
        return failCount;
    }

    public void setFailCount(long failCount) {
        this.failCount = failCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public long getOtherCount() {
        return otherCount;
    }

    public void setOtherCount(long otherCount) {
        this.otherCount = otherCount;
    }

    public long getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(long maxCount) {
        this.maxCount = Math.max(1, maxCount);
    }

    public long getTotalCount() {
        return successCount + failCount + errorCount + otherCount;
    }

    public String getSuccessHeightClass() {
        return heightClass(successCount);
    }

    public String getFailHeightClass() {
        return heightClass(failCount);
    }

    public String getErrorHeightClass() {
        return heightClass(errorCount);
    }

    public String getOtherHeightClass() {
        return heightClass(otherCount);
    }

    public String getSummaryText() {
        return "성공 " + successCount
                + " / 불일치 " + failCount
                + " / 오류 " + errorCount
                + " / 기타 " + otherCount;
    }

    private String heightClass(long count) {
        if (count <= 0) {
            return "h-0";
        }
        long percent = Math.max(8, Math.round((double) count / (double) maxCount * 100.0));
        long bucket = Math.max(5, Math.min(100, Math.round(percent / 5.0) * 5));
        return "h-" + bucket;
    }
}

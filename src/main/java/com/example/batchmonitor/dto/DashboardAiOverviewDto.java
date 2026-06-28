package com.example.batchmonitor.dto;

public class DashboardAiOverviewDto {

    private long totalValidationCount;
    private long successCount;
    private long mismatchCount;
    private long highRiskCount;
    private long aiAnalyzedCount;
    private long aiWarningSqlCount;

    public long getTotalValidationCount() {
        return totalValidationCount;
    }

    public void setTotalValidationCount(long totalValidationCount) {
        this.totalValidationCount = totalValidationCount;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public long getMismatchCount() {
        return mismatchCount;
    }

    public void setMismatchCount(long mismatchCount) {
        this.mismatchCount = mismatchCount;
    }

    public long getHighRiskCount() {
        return highRiskCount;
    }

    public void setHighRiskCount(long highRiskCount) {
        this.highRiskCount = highRiskCount;
    }

    public long getAiAnalyzedCount() {
        return aiAnalyzedCount;
    }

    public void setAiAnalyzedCount(long aiAnalyzedCount) {
        this.aiAnalyzedCount = aiAnalyzedCount;
    }

    public long getAiWarningSqlCount() {
        return aiWarningSqlCount;
    }

    public void setAiWarningSqlCount(long aiWarningSqlCount) {
        this.aiWarningSqlCount = aiWarningSqlCount;
    }
}

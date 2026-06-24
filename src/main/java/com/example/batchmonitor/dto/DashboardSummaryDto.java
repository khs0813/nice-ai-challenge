package com.example.batchmonitor.dto;

public class DashboardSummaryDto {

    private long totalJobCount;
    private long todayJobCount;
    private long completedJobCount;
    private long failedJobCount;
    private long runningJobCount;
    private long recentFailedJobCount;
    private long queryCompareSuccessCount;
    private long queryCompareFailCount;
    private String averageDurationText;
    private String latestJobName;

    public long getTotalJobCount() {
        return totalJobCount;
    }

    public void setTotalJobCount(long totalJobCount) {
        this.totalJobCount = totalJobCount;
    }

    public long getTodayJobCount() {
        return todayJobCount;
    }

    public void setTodayJobCount(long todayJobCount) {
        this.todayJobCount = todayJobCount;
    }

    public long getCompletedJobCount() {
        return completedJobCount;
    }

    public void setCompletedJobCount(long completedJobCount) {
        this.completedJobCount = completedJobCount;
    }

    public long getFailedJobCount() {
        return failedJobCount;
    }

    public void setFailedJobCount(long failedJobCount) {
        this.failedJobCount = failedJobCount;
    }

    public long getRunningJobCount() {
        return runningJobCount;
    }

    public void setRunningJobCount(long runningJobCount) {
        this.runningJobCount = runningJobCount;
    }

    public long getRecentFailedJobCount() {
        return recentFailedJobCount;
    }

    public void setRecentFailedJobCount(long recentFailedJobCount) {
        this.recentFailedJobCount = recentFailedJobCount;
    }

    public long getQueryCompareSuccessCount() {
        return queryCompareSuccessCount;
    }

    public void setQueryCompareSuccessCount(long queryCompareSuccessCount) {
        this.queryCompareSuccessCount = queryCompareSuccessCount;
    }

    public long getQueryCompareFailCount() {
        return queryCompareFailCount;
    }

    public void setQueryCompareFailCount(long queryCompareFailCount) {
        this.queryCompareFailCount = queryCompareFailCount;
    }

    public String getAverageDurationText() {
        return averageDurationText;
    }

    public void setAverageDurationText(String averageDurationText) {
        this.averageDurationText = averageDurationText;
    }

    public String getLatestJobName() {
        return latestJobName;
    }

    public void setLatestJobName(String latestJobName) {
        this.latestJobName = latestJobName;
    }
}

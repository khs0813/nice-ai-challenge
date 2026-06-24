package com.example.batchmonitor.dto;

public class DashboardValidationTypeDto {

    private String typeLabel;
    private long count;
    private long totalCount = 1;

    public String getTypeLabel() {
        return typeLabel;
    }

    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = Math.max(1, totalCount);
    }

    public String getPercentText() {
        return Math.round((double) count / (double) totalCount * 100.0) + "%";
    }

    public String getBarWidth() {
        if (count <= 0) {
            return "0%";
        }
        return Math.max(4, Math.round((double) count / (double) totalCount * 100.0)) + "%";
    }

    public String getBarWidthClass() {
        if (count <= 0) {
            return "w-0";
        }
        long percent = Math.max(5, Math.round((double) count / (double) totalCount * 100.0));
        long bucket = Math.max(5, Math.min(100, Math.round(percent / 5.0) * 5));
        return "w-" + bucket;
    }

    public String getDonutFillClass() {
        if (count <= 0) {
            return "d-0";
        }
        long percent = Math.round((double) count / (double) totalCount * 100.0);
        long bucket = Math.max(0, Math.min(100, Math.round(percent / 5.0) * 5));
        return "d-" + bucket;
    }

    public String getTotalText() {
        return totalCount + "건";
    }
}

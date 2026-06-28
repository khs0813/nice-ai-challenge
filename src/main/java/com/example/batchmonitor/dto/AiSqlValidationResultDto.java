package com.example.batchmonitor.dto;

import java.util.ArrayList;
import java.util.List;

public class AiSqlValidationResultDto {

    private String status = "PASS";
    private String riskLevel = "LOW";
    private List<String> detectedIssues = new ArrayList<String>();
    private List<String> recommendations = new ArrayList<String>();
    private boolean blocked;
    private boolean sample;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null) {
            this.status = "PASS";
            return;
        }
        String normalized = status.trim().toUpperCase();
        if ("WARN".equals(normalized)) {
            normalized = "WARNING";
        }
        if (!"PASS".equals(normalized) && !"WARNING".equals(normalized) && !"BLOCKED".equals(normalized)) {
            normalized = "PASS";
        }
        this.status = normalized;
        this.blocked = "BLOCKED".equals(normalized);
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        if (riskLevel == null) {
            this.riskLevel = "LOW";
            return;
        }
        String normalized = riskLevel.trim().toUpperCase();
        if (!"LOW".equals(normalized) && !"MEDIUM".equals(normalized) && !"HIGH".equals(normalized)) {
            normalized = "LOW";
        }
        this.riskLevel = normalized;
    }

    public List<String> getDetectedIssues() {
        return detectedIssues;
    }

    public void setDetectedIssues(List<String> detectedIssues) {
        this.detectedIssues = detectedIssues == null ? new ArrayList<String>() : detectedIssues;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations == null ? new ArrayList<String>() : recommendations;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
        if (blocked) {
            this.status = "BLOCKED";
            this.riskLevel = "HIGH";
        }
    }

    public boolean isSample() {
        return sample;
    }

    public void setSample(boolean sample) {
        this.sample = sample;
    }

    public int getWarningCount() {
        return "WARNING".equals(status) ? Math.max(1, detectedIssues.size()) : 0;
    }

    public int getBlockedCount() {
        return blocked ? 1 : 0;
    }

    public int getPassCount() {
        return "PASS".equals(status) ? 1 : 0;
    }

    public String getStatusLabel() {
        if ("BLOCKED".equals(status)) {
            return "차단";
        }
        if ("WARNING".equals(status)) {
            return "경고";
        }
        return "정상";
    }

    public String getStatusBadgeClass() {
        if ("BLOCKED".equals(status)) {
            return "severity-high";
        }
        if ("WARNING".equals(status)) {
            return "severity-medium";
        }
        return "severity-low";
    }
}

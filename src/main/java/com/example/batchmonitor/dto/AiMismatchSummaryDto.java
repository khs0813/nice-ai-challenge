package com.example.batchmonitor.dto;

import java.util.ArrayList;
import java.util.List;

public class AiMismatchSummaryDto {

    private Long resultId;
    private String severity = "LOW";
    private String summary;
    private List<String> suspectedCauses = new ArrayList<String>();
    private String businessImpact;
    private String recommendedAction;
    private String analysisTimeText;
    private String confidence = "MEDIUM";
    private boolean sample;

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = normalizeLevel(severity);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getSuspectedCauses() {
        return suspectedCauses;
    }

    public void setSuspectedCauses(List<String> suspectedCauses) {
        this.suspectedCauses = suspectedCauses == null ? new ArrayList<String>() : suspectedCauses;
    }

    public String getBusinessImpact() {
        return businessImpact;
    }

    public void setBusinessImpact(String businessImpact) {
        this.businessImpact = businessImpact;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public String getAnalysisTimeText() {
        return analysisTimeText;
    }

    public void setAnalysisTimeText(String analysisTimeText) {
        this.analysisTimeText = analysisTimeText;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = normalizeLevel(confidence);
    }

    public boolean isSample() {
        return sample;
    }

    public void setSample(boolean sample) {
        this.sample = sample;
    }

    public String getSeverityBadgeClass() {
        if ("HIGH".equals(severity)) {
            return "severity-high";
        }
        if ("MEDIUM".equals(severity)) {
            return "severity-medium";
        }
        return "severity-low";
    }

    public String getDetailUrl() {
        return resultId == null ? "/query-comparison-results" : "/query-comparison-results/" + resultId;
    }

    private String normalizeLevel(String value) {
        if (value == null) {
            return "LOW";
        }
        String normalized = value.trim().toUpperCase();
        if ("HIGH".equals(normalized) || "MEDIUM".equals(normalized) || "LOW".equals(normalized)) {
            return normalized;
        }
        return "LOW";
    }
}

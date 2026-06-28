package com.example.batchmonitor.dto;

import com.example.batchmonitor.util.DateTimeUtils;

import java.time.LocalDateTime;

public class QueryComparisonResultDto {

    private Long resultId;
    private Long comparisonId;
    private String comparisonName;
    private String requestedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String resultStatus;
    private Long sybaseRowCount;
    private Long oracleRowCount;
    private String sybaseResultHash;
    private String oracleResultHash;
    private String mismatchSummary;
    private String errorMessage;
    private String oracleBatchContext;
    private String aiAnalysisStatus;
    private String aiAnalysis;
    private String aiActionGuide;

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

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
        this.comparisonName = comparisonName;
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

    public Long getSybaseRowCount() {
        return sybaseRowCount;
    }

    public void setSybaseRowCount(Long sybaseRowCount) {
        this.sybaseRowCount = sybaseRowCount;
    }

    public Long getOracleRowCount() {
        return oracleRowCount;
    }

    public void setOracleRowCount(Long oracleRowCount) {
        this.oracleRowCount = oracleRowCount;
    }

    public String getSybaseResultHash() {
        return sybaseResultHash;
    }

    public void setSybaseResultHash(String sybaseResultHash) {
        this.sybaseResultHash = sybaseResultHash;
    }

    public String getOracleResultHash() {
        return oracleResultHash;
    }

    public void setOracleResultHash(String oracleResultHash) {
        this.oracleResultHash = oracleResultHash;
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

    public String getOracleBatchContext() {
        return oracleBatchContext;
    }

    public void setOracleBatchContext(String oracleBatchContext) {
        this.oracleBatchContext = oracleBatchContext;
    }

    public String getAiAnalysisStatus() {
        return aiAnalysisStatus;
    }

    public void setAiAnalysisStatus(String aiAnalysisStatus) {
        this.aiAnalysisStatus = aiAnalysisStatus;
    }

    public String getAiAnalysis() {
        return aiAnalysis;
    }

    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }

    public String getAiActionGuide() {
        return aiActionGuide;
    }

    public void setAiActionGuide(String aiActionGuide) {
        this.aiActionGuide = aiActionGuide;
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

    public String getMessageSummary() {
        String message = mismatchSummary != null ? mismatchSummary : errorMessage;
        return DateTimeUtils.summarize(message, 120);
    }

    public String getAiAnalysisSummary() {
        return DateTimeUtils.summarize(aiAnalysis, 120);
    }

    public String getAiAnalysisStatusLabel() {
        if (aiAnalysisStatus == null) {
            return "-";
        }
        String normalized = aiAnalysisStatus.toUpperCase();
        if ("SUCCESS".equals(normalized)) {
            return "분석 완료";
        }
        if ("SKIPPED".equals(normalized)) {
            return "분석 안 함";
        }
        if ("ERROR".equals(normalized)) {
            return "분석 실패";
        }
        return aiAnalysisStatus;
    }

    public String getAiAnalysisStatusShortLabel() {
        if (aiAnalysisStatus == null) {
            return "대기";
        }
        String normalized = aiAnalysisStatus.toUpperCase();
        if ("SUCCESS".equals(normalized)) {
            return "완료";
        }
        if ("ERROR".equals(normalized)) {
            return "실패";
        }
        if ("SKIPPED".equals(normalized)) {
            return "대기";
        }
        return aiAnalysisStatus;
    }

    public String getAiAnalysisStatusBadgeClass() {
        if (aiAnalysisStatus == null) {
            return "badge-unknown";
        }
        String normalized = aiAnalysisStatus.toUpperCase();
        if ("SUCCESS".equals(normalized)) {
            return "badge-completed";
        }
        if ("ERROR".equals(normalized)) {
            return "badge-failed";
        }
        return "badge-unknown";
    }

    public boolean hasAiSummary() {
        return aiAnalysis != null && !aiAnalysis.trim().isEmpty() && "SUCCESS".equalsIgnoreCase(aiAnalysisStatus);
    }

    public boolean isAiSummary() {
        return hasAiSummary();
    }

    public String getRiskLevel() {
        if ("ERROR".equalsIgnoreCase(resultStatus)) {
            return "HIGH";
        }
        if (sybaseRowCount != null && oracleRowCount != null) {
            long diff = Math.abs(sybaseRowCount - oracleRowCount);
            if (diff >= 5) {
                return "HIGH";
            }
            if (diff > 0) {
                return "MEDIUM";
            }
        }
        if ("FAIL".equalsIgnoreCase(resultStatus)) {
            return "MEDIUM";
        }
        return "LOW";
    }

    public String getRiskBadgeClass() {
        String riskLevel = getRiskLevel();
        if ("HIGH".equals(riskLevel)) {
            return "severity-high";
        }
        if ("MEDIUM".equals(riskLevel)) {
            return "severity-medium";
        }
        return "severity-low";
    }

    public String getResultStatusLabel() {
        return statusLabel(resultStatus);
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

    private String statusLabel(String status) {
        if (status == null) {
            return "-";
        }
        String normalized = status.toUpperCase();
        if ("SUCCESS".equals(normalized)) {
            return "일치";
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
        return status;
    }
}

package com.example.batchmonitor.service;

import com.example.batchmonitor.dto.AiMismatchSummaryDto;
import com.example.batchmonitor.dto.AiSqlValidationResultDto;
import com.example.batchmonitor.dto.DashboardAiOverviewDto;
import com.example.batchmonitor.dto.DashboardSummaryDto;
import com.example.batchmonitor.dto.DashboardValidationTrendDto;
import com.example.batchmonitor.dto.DashboardValidationTypeDto;
import com.example.batchmonitor.dto.JobExecutionDto;
import com.example.batchmonitor.dto.PageResult;
import com.example.batchmonitor.dto.QueryComparisonDto;
import com.example.batchmonitor.dto.QueryComparisonResultDto;
import com.example.batchmonitor.dto.QueryComparisonSearchConditionDto;
import com.example.batchmonitor.dto.SearchConditionDto;
import com.example.batchmonitor.dto.ValidationResultDto;
import com.example.batchmonitor.mapper.DashboardMapper;
import com.example.batchmonitor.mapper.QueryComparisonMapper;
import com.example.batchmonitor.mapper.ValidationResultMapper;
import com.example.batchmonitor.util.DateTimeUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final DashboardMapper dashboardMapper;
    private final QueryComparisonMapper queryComparisonMapper;
    private final ValidationResultMapper validationResultMapper;

    public DashboardService(DashboardMapper dashboardMapper,
                            QueryComparisonMapper queryComparisonMapper,
                            ValidationResultMapper validationResultMapper) {
        this.dashboardMapper = dashboardMapper;
        this.queryComparisonMapper = queryComparisonMapper;
        this.validationResultMapper = validationResultMapper;
    }

    public DashboardSummaryDto getSummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime recentFrom = LocalDateTime.now().minusDays(7);

        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setTotalJobCount(dashboardMapper.countTotalJobs());
        summary.setTodayJobCount(dashboardMapper.countTodayJobs(todayStart, tomorrowStart));
        summary.setCompletedJobCount(dashboardMapper.countCompletedJobs());
        summary.setFailedJobCount(dashboardMapper.countFailedJobs());
        summary.setRunningJobCount(dashboardMapper.countRunningJobs());
        summary.setRecentFailedJobCount(dashboardMapper.countRecentFailedJobs(recentFrom));
        summary.setQueryCompareSuccessCount(countValidationResults("SUCCESS"));
        summary.setQueryCompareFailCount(countValidationResults("FAIL"));
        summary.setLatestJobName(defaultText(dashboardMapper.findLatestJobName()));
        summary.setAverageDurationText(calculateAverageDurationText(dashboardMapper.findCompletedExecutionsForAverage(1000)));
        return summary;
    }

    public List<JobExecutionDto> getRecentJobExecutions() {
        List<JobExecutionDto> jobs = dashboardMapper.findRecentJobExecutions(10);
        enrichJobs(jobs);
        return jobs;
    }

    public List<QueryComparisonResultDto> getRecentQueryComparisonResults() {
        QueryComparisonSearchConditionDto condition = new QueryComparisonSearchConditionDto();
        condition.setSize(10);
        condition.normalizePaging();
        return queryComparisonMapper.findResults(condition);
    }

    public DashboardAiOverviewDto getAiOverview() {
        DashboardAiOverviewDto overview = new DashboardAiOverviewDto();
        List<ValidationResultDto> todayResults = findRecentValidationResults(LocalDate.now().atStartOfDay());
        List<QueryComparisonResultDto> recentQueryResults = getRecentQueryComparisonResults();
        List<QueryComparisonDto> comparisons = findRecentComparisons();

        overview.setTotalValidationCount(todayResults.size());
        for (ValidationResultDto result : todayResults) {
            if ("SUCCESS".equalsIgnoreCase(result.getResultStatus())) {
                overview.setSuccessCount(overview.getSuccessCount() + 1);
            } else if ("FAIL".equalsIgnoreCase(result.getResultStatus()) || "ERROR".equalsIgnoreCase(result.getResultStatus())) {
                overview.setMismatchCount(overview.getMismatchCount() + 1);
            }
        }
        for (QueryComparisonResultDto result : recentQueryResults) {
            if ("SUCCESS".equalsIgnoreCase(result.getAiAnalysisStatus())) {
                overview.setAiAnalyzedCount(overview.getAiAnalyzedCount() + 1);
            }
            if (isHighRisk(result)) {
                overview.setHighRiskCount(overview.getHighRiskCount() + 1);
            }
        }
        for (QueryComparisonDto comparison : comparisons) {
            if ("WARN".equalsIgnoreCase(comparison.getAiSqlReviewStatus())
                    || "FAIL".equalsIgnoreCase(comparison.getAiSqlReviewStatus())) {
                overview.setAiWarningSqlCount(overview.getAiWarningSqlCount() + 1);
            }
        }

        if (overview.getTotalValidationCount() == 0 && recentQueryResults.isEmpty()) {
            overview.setTotalValidationCount(24);
            overview.setSuccessCount(21);
            overview.setMismatchCount(3);
            overview.setHighRiskCount(1);
            overview.setAiAnalyzedCount(12);
            overview.setAiWarningSqlCount(2);
        }
        return overview;
    }

    public AiMismatchSummaryDto getAiMismatchSummary() {
        List<QueryComparisonResultDto> results = getRecentQueryComparisonResults();
        for (QueryComparisonResultDto result : results) {
            if (("FAIL".equalsIgnoreCase(result.getResultStatus()) || "ERROR".equalsIgnoreCase(result.getResultStatus()))
                    && "SUCCESS".equalsIgnoreCase(result.getAiAnalysisStatus())) {
                return buildMismatchSummary(result);
            }
        }
        return sampleMismatchSummary();
    }

    public AiSqlValidationResultDto getAiSqlValidationResult() {
        List<QueryComparisonDto> comparisons = findRecentComparisons();
        for (QueryComparisonDto comparison : comparisons) {
            if ("FAIL".equalsIgnoreCase(comparison.getAiSqlReviewStatus())
                    || "WARN".equalsIgnoreCase(comparison.getAiSqlReviewStatus())) {
                return buildSqlValidationResult(comparison);
            }
        }
        return sampleSqlValidationResult();
    }

    public List<DashboardValidationTrendDto> getValidationTrend() {
        LocalDate today = LocalDate.now();
        Map<LocalDate, DashboardValidationTrendDto> trendMap = new LinkedHashMap<LocalDate, DashboardValidationTrendDto>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            DashboardValidationTrendDto trend = new DashboardValidationTrendDto();
            trend.setDateLabel(date.format(formatter));
            trendMap.put(date, trend);
        }

        List<ValidationResultDto> results = findRecentValidationResults(today.minusDays(6).atStartOfDay());
        long maxCount = 1;
        for (ValidationResultDto result : results) {
            if (result.getRequestedAt() == null) {
                continue;
            }
            DashboardValidationTrendDto trend = trendMap.get(result.getRequestedAt().toLocalDate());
            if (trend == null) {
                continue;
            }
            if ("SUCCESS".equalsIgnoreCase(result.getResultStatus())) {
                trend.setSuccessCount(trend.getSuccessCount() + 1);
            } else if ("FAIL".equalsIgnoreCase(result.getResultStatus())) {
                trend.setFailCount(trend.getFailCount() + 1);
            } else if ("ERROR".equalsIgnoreCase(result.getResultStatus())) {
                trend.setErrorCount(trend.getErrorCount() + 1);
            } else {
                trend.setOtherCount(trend.getOtherCount() + 1);
            }
            maxCount = Math.max(maxCount, trend.getTotalCount());
        }

        List<DashboardValidationTrendDto> trends = new ArrayList<DashboardValidationTrendDto>(trendMap.values());
        for (DashboardValidationTrendDto trend : trends) {
            trend.setMaxCount(maxCount);
        }
        return trends;
    }

    public long getValidationTrendTotalCount(List<DashboardValidationTrendDto> trends) {
        long total = 0;
        if (trends == null) {
            return total;
        }
        for (DashboardValidationTrendDto trend : trends) {
            total += trend.getTotalCount();
        }
        return total;
    }

    public List<DashboardValidationTypeDto> getValidationTypeSummary() {
        List<ValidationResultDto> results = findRecentValidationResults(LocalDate.now().minusDays(6).atStartOfDay());
        long pairCount = 0;
        long singleCount = 0;
        for (ValidationResultDto result : results) {
            if ("SINGLE".equalsIgnoreCase(result.getValidationType())) {
                singleCount++;
            } else if ("PAIR".equalsIgnoreCase(result.getValidationType())) {
                pairCount++;
            }
        }
        long totalCount = Math.max(1, pairCount + singleCount);
        List<DashboardValidationTypeDto> summaries = new ArrayList<DashboardValidationTypeDto>();
        summaries.add(typeSummary("DB 간 비교", pairCount, totalCount));
        summaries.add(typeSummary("기대값 검증", singleCount, totalCount));
        return summaries;
    }

    public PageResult<JobExecutionDto> getDetailJobs(String type, SearchConditionDto condition) {
        condition.normalizePaging();
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime recentFrom = LocalDateTime.now().minusDays(7);

        List<JobExecutionDto> jobs;
        long totalCount;
        if ("today".equals(type)) {
            jobs = dashboardMapper.findTodayJobExecutions(todayStart, tomorrowStart, condition.getOffset(), condition.getSize());
            totalCount = dashboardMapper.countTodayJobs(todayStart, tomorrowStart);
        } else if ("completed".equals(type)) {
            jobs = dashboardMapper.findCompletedJobExecutions(condition.getOffset(), condition.getSize());
            totalCount = dashboardMapper.countCompletedJobs();
        } else if ("failed".equals(type)) {
            jobs = dashboardMapper.findFailedJobExecutions(condition.getOffset(), condition.getSize());
            totalCount = dashboardMapper.countFailedJobs();
        } else if ("running".equals(type)) {
            jobs = dashboardMapper.findRunningJobExecutions(condition.getOffset(), condition.getSize());
            totalCount = dashboardMapper.countRunningJobs();
        } else if ("recent-failed".equals(type)) {
            jobs = dashboardMapper.findRecentFailedJobExecutions(recentFrom, condition.getOffset(), condition.getSize());
            totalCount = dashboardMapper.countRecentFailedJobs(recentFrom);
        } else {
            jobs = dashboardMapper.findTotalJobExecutions(condition.getOffset(), condition.getSize());
            totalCount = dashboardMapper.countTotalJobs();
        }

        enrichJobs(jobs);
        return new PageResult<JobExecutionDto>(jobs, totalCount, condition.getPage(), condition.getSize());
    }

    public String getDetailTitle(String type) {
        if ("today".equals(type)) {
            return "오늘 실행";
        }
        if ("completed".equals(type)) {
            return "성공 배치";
        }
        if ("failed".equals(type)) {
            return "실패 배치";
        }
        if ("running".equals(type)) {
            return "실행 중 배치";
        }
        if ("recent-failed".equals(type)) {
            return "최근 7일 실패 배치";
        }
        return "전체 배치 실행";
    }

    public String normalizeDetailType(String type) {
        if ("today".equals(type)
                || "completed".equals(type)
                || "failed".equals(type)
                || "running".equals(type)
                || "recent-failed".equals(type)) {
            return type;
        }
        return "total";
    }

    private void enrichJobs(List<JobExecutionDto> jobs) {
        if (jobs == null) {
            return;
        }
        for (JobExecutionDto job : jobs) {
            job.setDurationText(DateTimeUtils.durationText(job.getStartTime(), job.getEndTime()));
        }
    }

    private String calculateAverageDurationText(List<JobExecutionDto> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return "-";
        }
        long count = 0;
        long totalSeconds = 0;
        for (JobExecutionDto job : jobs) {
            if (job.getStartTime() != null && job.getEndTime() != null) {
                Duration duration = Duration.between(job.getStartTime(), job.getEndTime());
                if (!duration.isNegative()) {
                    totalSeconds += duration.getSeconds();
                    count++;
                }
            }
        }
        if (count == 0) {
            return "-";
        }
        return DateTimeUtils.durationText(Duration.ofSeconds(totalSeconds / count));
    }

    private String defaultText(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private long countValidationResults(String status) {
        QueryComparisonSearchConditionDto condition = new QueryComparisonSearchConditionDto();
        condition.setResultStatus(status);
        condition.normalizePaging();
        return validationResultMapper.countResults(condition);
    }

    private List<ValidationResultDto> findRecentValidationResults(LocalDateTime startDateTime) {
        QueryComparisonSearchConditionDto condition = new QueryComparisonSearchConditionDto();
        condition.setPage(1);
        condition.setSize(100);
        condition.normalizePaging();
        List<ValidationResultDto> results = validationResultMapper.findResults(condition);
        List<ValidationResultDto> filtered = new ArrayList<ValidationResultDto>();
        if (results == null) {
            return filtered;
        }
        for (ValidationResultDto result : results) {
            if (result.getRequestedAt() != null && !result.getRequestedAt().isBefore(startDateTime)) {
                filtered.add(result);
            }
        }
        return filtered;
    }

    private List<QueryComparisonDto> findRecentComparisons() {
        QueryComparisonSearchConditionDto condition = new QueryComparisonSearchConditionDto();
        condition.setPage(1);
        condition.setSize(100);
        condition.normalizePaging();
        return queryComparisonMapper.findComparisons(condition);
    }

    private AiMismatchSummaryDto buildMismatchSummary(QueryComparisonResultDto result) {
        AiMismatchSummaryDto summary = new AiMismatchSummaryDto();
        summary.setResultId(result.getResultId());
        summary.setSeverity(isHighRisk(result) ? "HIGH" : "MEDIUM");
        summary.setSummary(defaultText(result.getMismatchSummary() != null ? result.getMismatchSummary() : result.getErrorMessage()));
        List<String> causes = new ArrayList<String>();
        String analysis = result.getAiAnalysis() == null ? "" : result.getAiAnalysis();
        if (analysis.contains("배치") || analysis.contains("반영")) {
            causes.add("배치 적재 지연 또는 Oracle 반영 누락 가능성");
        }
        if (analysis.contains("조건") || analysis.contains("필터")) {
            causes.add("DB 간 필터 조건 또는 상태값 매핑 차이 가능성");
        }
        if (causes.isEmpty()) {
            causes.add("동일 시간대 배치 상태와 비교 SQL 기준 확인 필요");
        }
        summary.setSuspectedCauses(causes);
        summary.setBusinessImpact("정산 집계 또는 대상 건수 불일치로 후속 업무 금액 검증에 영향이 있을 수 있습니다.");
        summary.setRecommendedAction(defaultText(result.getAiActionGuide() != null ? result.getAiActionGuide() : "누락 키와 배치 실행 로그를 확인한 뒤 재처리 여부를 검토하세요."));
        summary.setConfidence("MEDIUM");
        return summary;
    }

    private AiSqlValidationResultDto buildSqlValidationResult(QueryComparisonDto comparison) {
        AiSqlValidationResultDto result = new AiSqlValidationResultDto();
        if ("FAIL".equalsIgnoreCase(comparison.getAiSqlReviewStatus())) {
            result.setStatus("BLOCKED");
        } else {
            result.setStatus("WARNING");
            result.setRiskLevel("MEDIUM");
        }
        result.getDetectedIssues().add(defaultText(extractFirstReviewSentence(comparison.getAiSqlReviewSummary())));
        result.getRecommendations().add("룰 기반 차단 항목과 날짜/조건/비교 기준 컬럼을 확인한 뒤 SQL을 다시 검토하세요.");
        return result;
    }

    private AiMismatchSummaryDto sampleMismatchSummary() {
        AiMismatchSummaryDto summary = new AiMismatchSummaryDto();
        summary.setSample(true);
        summary.setSeverity("HIGH");
        summary.setSummary("Oracle 데이터가 Sybase 대비 6건 부족합니다.");
        List<String> causes = new ArrayList<String>();
        causes.add("배치 적재 지연");
        causes.add("상태값 매핑 오류");
        summary.setSuspectedCauses(causes);
        summary.setBusinessImpact("일 정산 금액 불일치 가능성이 있습니다.");
        summary.setRecommendedAction("거래번호 기준 누락 건 확인 후 재처리 여부를 검토하세요.");
        summary.setConfidence("MEDIUM");
        return summary;
    }

    private AiSqlValidationResultDto sampleSqlValidationResult() {
        AiSqlValidationResultDto result = new AiSqlValidationResultDto();
        result.setSample(true);
        result.setStatus("WARNING");
        result.setRiskLevel("MEDIUM");
        result.getDetectedIssues().add("날짜 조건이 없어 대량 조회 가능성이 있습니다.");
        result.getDetectedIssues().add("비교 기준 컬럼이 명확하지 않습니다.");
        result.getDetectedIssues().add("Oracle/Sybase 결과 컬럼 수가 다를 수 있습니다.");
        result.getRecommendations().add("거래일자 조건과 비교 기준 키를 추가하세요.");
        result.getRecommendations().add("Sybase와 Oracle 쿼리의 결과 컬럼명과 컬럼 수를 맞추세요.");
        return result;
    }

    private boolean isHighRisk(QueryComparisonResultDto result) {
        if (result == null) {
            return false;
        }
        if ("ERROR".equalsIgnoreCase(result.getResultStatus())) {
            return true;
        }
        if (result.getSybaseRowCount() != null && result.getOracleRowCount() != null) {
            return Math.abs(result.getSybaseRowCount() - result.getOracleRowCount()) >= 5;
        }
        return false;
    }

    private String extractFirstReviewSentence(String review) {
        if (review == null) {
            return null;
        }
        String normalized = review.replaceFirst("(?is)^\\s*(PASS|WARN|FAIL)\\s*[:\\-]?\\s*", "").trim();
        int bullet = normalized.indexOf(" - ");
        if (bullet >= 0) {
            normalized = normalized.substring(0, bullet);
        }
        return normalized.length() > 140 ? normalized.substring(0, 140) : normalized;
    }

    private DashboardValidationTypeDto typeSummary(String label, long count, long totalCount) {
        DashboardValidationTypeDto summary = new DashboardValidationTypeDto();
        summary.setTypeLabel(label);
        summary.setCount(count);
        summary.setTotalCount(totalCount);
        return summary;
    }
}

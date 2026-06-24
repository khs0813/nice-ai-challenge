package com.example.batchmonitor.service;

import com.example.batchmonitor.dto.DashboardSummaryDto;
import com.example.batchmonitor.dto.DashboardValidationTrendDto;
import com.example.batchmonitor.dto.DashboardValidationTypeDto;
import com.example.batchmonitor.dto.JobExecutionDto;
import com.example.batchmonitor.dto.PageResult;
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

    private DashboardValidationTypeDto typeSummary(String label, long count, long totalCount) {
        DashboardValidationTypeDto summary = new DashboardValidationTypeDto();
        summary.setTypeLabel(label);
        summary.setCount(count);
        summary.setTotalCount(totalCount);
        return summary;
    }
}

package com.example.batchmonitor.service;

import com.example.batchmonitor.dto.QueryComparisonDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QueryComparisonScheduler {

    private static final Logger log = LoggerFactory.getLogger(QueryComparisonScheduler.class);

    private final QueryComparisonService queryComparisonService;
    private final Set<Long> runningComparisonIds = ConcurrentHashMap.newKeySet();

    public QueryComparisonScheduler(QueryComparisonService queryComparisonService) {
        this.queryComparisonService = queryComparisonService;
    }

    @Scheduled(fixedDelayString = "${query.compare.scheduler.fixed-delay-ms:60000}")
    public void runDueComparisons() {
        List<QueryComparisonDto> comparisons = queryComparisonService.findScheduledComparisons();
        LocalDateTime now = LocalDateTime.now();
        for (QueryComparisonDto comparison : comparisons) {
            Long comparisonId = comparison.getComparisonId();
            if (comparisonId == null || !runningComparisonIds.add(comparisonId)) {
                continue;
            }
            try {
                if (queryComparisonService.isScheduleDue(comparison, now)) {
                    queryComparisonService.requestComparison(comparisonId, "scheduler");
                }
            } catch (Exception e) {
                log.error("Scheduled query comparison failed. comparisonId={}", comparisonId, e);
            } finally {
                runningComparisonIds.remove(comparisonId);
            }
        }
    }
}

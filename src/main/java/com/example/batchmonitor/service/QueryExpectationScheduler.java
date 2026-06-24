package com.example.batchmonitor.service;

import com.example.batchmonitor.dto.QueryExpectationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QueryExpectationScheduler {

    private static final Logger log = LoggerFactory.getLogger(QueryExpectationScheduler.class);

    private final QueryExpectationService queryExpectationService;
    private final Set<Long> runningExpectationIds = ConcurrentHashMap.newKeySet();

    public QueryExpectationScheduler(QueryExpectationService queryExpectationService) {
        this.queryExpectationService = queryExpectationService;
    }

    @Scheduled(fixedDelayString = "${query.compare.scheduler.fixed-delay-ms:60000}")
    public void runDueExpectations() {
        List<QueryExpectationDto> expectations = queryExpectationService.findScheduledExpectations();
        LocalDateTime now = LocalDateTime.now();
        for (QueryExpectationDto expectation : expectations) {
            Long expectationId = expectation.getExpectationId();
            if (expectationId == null || !runningExpectationIds.add(expectationId)) {
                continue;
            }
            try {
                if (queryExpectationService.isScheduleDue(expectation, now)) {
                    queryExpectationService.requestExpectation(expectationId, "scheduler");
                }
            } catch (Exception e) {
                log.error("Scheduled query expectation failed. expectationId={}", expectationId, e);
            } finally {
                runningExpectationIds.remove(expectationId);
            }
        }
    }
}

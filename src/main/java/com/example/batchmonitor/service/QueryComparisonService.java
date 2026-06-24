package com.example.batchmonitor.service;

import com.example.batchmonitor.config.QueryCompareProperties;
import com.example.batchmonitor.dto.PageResult;
import com.example.batchmonitor.dto.JobExecutionDto;
import com.example.batchmonitor.dto.QueryComparisonDto;
import com.example.batchmonitor.dto.QueryComparisonResultDto;
import com.example.batchmonitor.dto.QueryComparisonSearchConditionDto;
import com.example.batchmonitor.dto.SearchConditionDto;
import com.example.batchmonitor.mapper.JobExecutionMapper;
import com.example.batchmonitor.mapper.QueryComparisonMapper;
import com.example.batchmonitor.util.DateTimeUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class QueryComparisonService {

    private static final int MAX_COMPARE_ROWS = 10000;

    private final QueryComparisonMapper queryComparisonMapper;
    private final JdbcTemplate sybaseJdbcTemplate;
    private final JdbcTemplate oracleJdbcTemplate;
    private final ValidationNotificationService validationNotificationService;
    private final JobExecutionMapper jobExecutionMapper;
    private final OpenAiService openAiService;
    private final QueryCompareProperties queryCompareProperties;

    public QueryComparisonService(QueryComparisonMapper queryComparisonMapper,
                                  DataSource dataSource,
                                  @Qualifier("sybaseQueryJdbcTemplate") ObjectProvider<JdbcTemplate> sybaseJdbcTemplateProvider,
                                  @Qualifier("oracleQueryJdbcTemplate") ObjectProvider<JdbcTemplate> oracleJdbcTemplateProvider,
                                  ValidationNotificationService validationNotificationService,
                                  JobExecutionMapper jobExecutionMapper,
                                  OpenAiService openAiService,
                                  QueryCompareProperties queryCompareProperties) {
        this.queryComparisonMapper = queryComparisonMapper;
        JdbcTemplate defaultJdbcTemplate = new JdbcTemplate(dataSource);
        this.sybaseJdbcTemplate = sybaseJdbcTemplateProvider.getIfAvailable(new ObjectProviderFallback(defaultJdbcTemplate));
        this.oracleJdbcTemplate = oracleJdbcTemplateProvider.getIfAvailable(new ObjectProviderFallback(defaultJdbcTemplate));
        this.validationNotificationService = validationNotificationService;
        this.jobExecutionMapper = jobExecutionMapper;
        this.openAiService = openAiService;
        this.queryCompareProperties = queryCompareProperties;
    }

    public PageResult<QueryComparisonDto> findComparisons(QueryComparisonSearchConditionDto condition) {
        prepare(condition);
        return new PageResult<QueryComparisonDto>(
                queryComparisonMapper.findComparisons(condition),
                queryComparisonMapper.countComparisons(condition),
                condition.getPage(),
                condition.getSize()
        );
    }

    public QueryComparisonDto findComparisonById(Long comparisonId) {
        QueryComparisonDto comparison = queryComparisonMapper.findComparisonById(comparisonId);
        if (comparison == null) {
            throw new IllegalArgumentException("DB 간 비교 설정을 찾을 수 없습니다. 검증 번호=" + comparisonId);
        }
        return comparison;
    }

    @Transactional
    public void saveComparison(QueryComparisonDto comparison, String username) {
        validateComparison(comparison);
        validateComparisonQueries(comparison);
        if (comparison.getCompareMode() == null) {
            comparison.setCompareMode("EXACT");
        }
        if (comparison.getComparisonId() == null) {
            comparison.setCreatedBy(username);
            comparison.setUpdatedBy(username);
            queryComparisonMapper.insertComparison(comparison);
            return;
        }
        comparison.setUpdatedBy(username);
        queryComparisonMapper.updateComparison(comparison);
    }

    @Transactional
    public void deleteComparison(Long comparisonId) {
        queryComparisonMapper.deleteComparison(comparisonId);
    }

    @Transactional
    public Long requestComparison(Long comparisonId, String username) {
        QueryComparisonDto comparison = findComparisonById(comparisonId);
        if (!"Y".equalsIgnoreCase(comparison.getEnabledYn())) {
            throw new IllegalArgumentException("중지된 DB 간 비교는 실행할 수 없습니다.");
        }

        LocalDateTime requestedAt = LocalDateTime.now(queryCompareProperties.getSchedulerZoneId());
        LocalDateTime startedAt = LocalDateTime.now(queryCompareProperties.getSchedulerZoneId());
        QueryComparisonResultDto result = new QueryComparisonResultDto();
        result.setComparisonId(comparisonId);
        result.setRequestedBy(username);
        result.setRequestedAt(requestedAt);
        result.setStartedAt(startedAt);

        try {
            validateExecutableSelect(comparison.getSybaseQuery(), "Sybase");
            validateExecutableSelect(comparison.getOracleQuery(), "Oracle");

            List<Map<String, Object>> sybaseRows = sybaseJdbcTemplate.queryForList(normalizeSql(comparison.getSybaseQuery()));
            List<Map<String, Object>> oracleRows = oracleJdbcTemplate.queryForList(normalizeSql(comparison.getOracleQuery()));
            result.setSybaseRowCount((long) sybaseRows.size());
            result.setOracleRowCount((long) oracleRows.size());

            if (sybaseRows.size() > MAX_COMPARE_ROWS || oracleRows.size() > MAX_COMPARE_ROWS) {
                result.setResultStatus("ERROR");
                result.setErrorMessage("비교 가능한 최대 행 수는 " + MAX_COMPARE_ROWS + "건입니다. Sybase="
                        + sybaseRows.size() + ", Oracle=" + oracleRows.size());
            } else {
                String sybaseCanonical = canonicalize(sybaseRows);
                String oracleCanonical = canonicalize(oracleRows);
                result.setSybaseResultHash(sha256(sybaseCanonical));
                result.setOracleResultHash(sha256(oracleCanonical));

                if (sybaseCanonical.equals(oracleCanonical)) {
                    result.setResultStatus("SUCCESS");
                    result.setMismatchSummary("두 쿼리 결과가 일치합니다.");
                } else {
                    result.setResultStatus("FAIL");
                    result.setMismatchSummary(buildMismatchSummary(sybaseRows, oracleRows));
                }
            }
        } catch (Exception e) {
            result.setResultStatus("ERROR");
            result.setErrorMessage(e.getMessage());
        }

        result.setFinishedAt(LocalDateTime.now(queryCompareProperties.getSchedulerZoneId()));
        enrichAiAnalysisIfNeeded(comparison, result);
        queryComparisonMapper.insertResult(result);
        notifyIfNeeded(comparison, result);
        return result.getResultId();
    }

    public QueryComparisonResultDto findResultById(Long resultId) {
        return findResultById(resultId, false);
    }

    public QueryComparisonResultDto findResultDetail(Long resultId) {
        return findResultById(resultId, true);
    }

    private QueryComparisonResultDto findResultById(Long resultId, boolean required) {
        if (resultId == null) {
            if (required) {
                throw new IllegalArgumentException("DB 간 비교 결과 번호가 필요합니다.");
            }
            return null;
        }
        QueryComparisonResultDto result = queryComparisonMapper.findResultById(resultId);
        if (result == null && required) {
            throw new IllegalArgumentException("DB 간 비교 결과를 찾을 수 없습니다. 결과 번호=" + resultId);
        }
        return result;
    }

    public QueryComparisonResultDto findLatestResultByComparisonId(Long comparisonId) {
        if (comparisonId == null) {
            return null;
        }
        return queryComparisonMapper.findLatestResultByComparisonId(comparisonId);
    }

    public List<QueryComparisonDto> findScheduledComparisons() {
        return queryComparisonMapper.findScheduledComparisons();
    }

    public boolean isScheduleDue(QueryComparisonDto comparison, LocalDateTime now) {
        if (comparison == null || !"Y".equalsIgnoreCase(comparison.getScheduleEnabledYn())) {
            return false;
        }
        if (comparison.getCronExpression() == null) {
            return false;
        }
        QueryComparisonResultDto latestResult = findLatestResultByComparisonId(comparison.getComparisonId());
        ZoneId schedulerZoneId = queryCompareProperties.getSchedulerZoneId();
        CronSequenceGenerator cron = new CronSequenceGenerator(
                comparison.getCronExpression(),
                java.util.TimeZone.getTimeZone(schedulerZoneId)
        );
        LocalDateTime baseTime = latestResult != null && latestResult.getRequestedAt() != null
                ? latestResult.getRequestedAt()
                : comparison.getUpdatedAt() != null ? comparison.getUpdatedAt() : comparison.getCreatedAt();
        if (baseTime == null) {
            baseTime = now;
        }
        Date base = Date.from(baseTime.atZone(schedulerZoneId).toInstant());
        Date next = cron.next(base);
        Date current = Date.from(now.atZone(schedulerZoneId).toInstant());
        return !next.after(current);
    }

    public PageResult<QueryComparisonResultDto> findResults(QueryComparisonSearchConditionDto condition) {
        prepare(condition);
        return new PageResult<QueryComparisonResultDto>(
                queryComparisonMapper.findResults(condition),
                queryComparisonMapper.countResults(condition),
                condition.getPage(),
                condition.getSize()
        );
    }

    private void prepare(QueryComparisonSearchConditionDto condition) {
        condition.normalizePaging();
        condition.setStartDateTime(DateTimeUtils.parseStartDate(condition.getStartDate()));
        condition.setEndDateTime(DateTimeUtils.parseEndDateExclusive(condition.getEndDate()));
    }

    private void validateComparison(QueryComparisonDto comparison) {
        if (comparison.getComparisonName() == null) {
            throw new IllegalArgumentException("비교명은 필수입니다.");
        }
        if (comparison.getSybaseQuery() == null) {
            throw new IllegalArgumentException("Sybase 검증 SQL은 필수입니다.");
        }
        if (comparison.getOracleQuery() == null) {
            throw new IllegalArgumentException("Oracle 검증 SQL은 필수입니다.");
        }
        String compareMode = comparison.getCompareMode();
        if (compareMode != null && !"EXACT".equals(compareMode) && !"HASH".equals(compareMode)) {
            throw new IllegalArgumentException("지원하지 않는 비교 방식입니다.");
        }
        if ("Y".equalsIgnoreCase(comparison.getScheduleEnabledYn())) {
            if (comparison.getCronExpression() == null) {
                throw new IllegalArgumentException("스케줄을 사용하려면 자동 실행 주기가 필요합니다.");
            }
            try {
                new CronSequenceGenerator(comparison.getCronExpression());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("자동 실행 주기 형식이 올바르지 않습니다. 예: 0 */10 * * * *", e);
            }
        }
    }

    private void validateComparisonQueries(QueryComparisonDto comparison) {
        validateExecutableSelect(comparison.getSybaseQuery(), "Sybase");
        validateExecutableSelect(comparison.getOracleQuery(), "Oracle");
        validateQueryOnDatabase(sybaseJdbcTemplate, comparison.getSybaseQuery(), "Sybase");
        validateQueryOnDatabase(oracleJdbcTemplate, comparison.getOracleQuery(), "Oracle");
        reviewSqlWithAiIfEnabled(comparison);
    }

    private void validateQueryOnDatabase(JdbcTemplate jdbcTemplate, String query, String label) {
        final String sql = normalizeSql(query);
        try {
            jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setMaxRows(1);
                    statement.setFetchSize(1);
                    statement.executeQuery();
                }
                return null;
            });
        } catch (DataAccessException e) {
            throw new IllegalArgumentException(label + " 검증 SQL 확인에 실패했습니다: " + rootMessage(e), e);
        }
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage();
    }

    private void validateExecutableSelect(String query, String label) {
        if (query == null) {
            throw new IllegalArgumentException(label + " 검증 SQL은 필수입니다.");
        }
        String normalized = query.trim().toLowerCase();
        if (!normalized.startsWith("select") && !normalized.startsWith("with")) {
            throw new IllegalArgumentException(label + " 검증 SQL은 SELECT 또는 WITH 조회문만 실행할 수 있습니다.");
        }
        String padded = " " + normalized + " ";
        String[] banned = {" insert ", " update ", " delete ", " merge ", " drop ", " alter ",
                " truncate ", " grant ", " revoke ", " execute ", " call "};
        for (String keyword : banned) {
            if (padded.contains(keyword)) {
                throw new IllegalArgumentException(label + " 검증 SQL에 허용되지 않는 명령어가 포함되어 있습니다: " + keyword.trim());
            }
        }
    }

    private String normalizeSql(String query) {
        String normalized = query.trim();
        while (normalized.endsWith(";")) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private String canonicalize(List<Map<String, Object>> rows) {
        List<String> canonicalRows = new ArrayList<String>();
        for (Map<String, Object> row : rows) {
            TreeMap<String, Object> sorted = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
            sorted.putAll(row);
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, Object> entry : sorted.entrySet()) {
                builder.append(entry.getKey().toUpperCase()).append('=').append(String.valueOf(entry.getValue())).append('|');
            }
            canonicalRows.add(builder.toString());
        }
        return join(canonicalRows);
    }

    private String buildMismatchSummary(List<Map<String, Object>> sybaseRows, List<Map<String, Object>> oracleRows) {
        if (sybaseRows.size() != oracleRows.size()) {
            return "조회 건수가 다릅니다. Sybase=" + sybaseRows.size() + "건, Oracle=" + oracleRows.size() + "건";
        }
        for (int i = 0; i < sybaseRows.size(); i++) {
            String sybaseRow = canonicalizeSingle(sybaseRows.get(i));
            String oracleRow = canonicalizeSingle(oracleRows.get(i));
            if (!sybaseRow.equals(oracleRow)) {
                return (i + 1) + "번째 행의 값이 다릅니다. Sybase=[" + summarize(sybaseRow) + "], Oracle=["
                        + summarize(oracleRow) + "]";
            }
        }
        return "조회 결과 식별값이 다릅니다.";
    }

    private String canonicalizeSingle(Map<String, Object> row) {
        TreeMap<String, Object> sorted = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(row);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            builder.append(entry.getKey().toUpperCase()).append('=').append(String.valueOf(entry.getValue())).append('|');
        }
        return builder.toString();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    private String join(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            builder.append(value).append('\n');
        }
        return builder.toString();
    }

    private String summarize(String value) {
        return DateTimeUtils.summarize(value, 180);
    }

    private void reviewSqlWithAiIfEnabled(QueryComparisonDto comparison) {
        if (!"Y".equalsIgnoreCase(comparison.getAiSqlReviewYn())) {
            comparison.setAiSqlReviewStatus(null);
            comparison.setAiSqlReviewSummary(null);
            return;
        }
        String prompt = "비교명: " + comparison.getComparisonName()
                + "\n설명: " + nullSafe(comparison.getDescription())
                + "\n\n[Sybase SQL]\n" + comparison.getSybaseQuery()
                + "\n\n[Oracle SQL]\n" + comparison.getOracleQuery();
        String instructions = "너는 금융/정산 데이터 정합성 검증 SQL 리뷰어다. "
                + "두 SQL이 같은 업무 기준을 비교하는지 점검한다. "
                + "위험 DML/DDL, 날짜/상태 조건 누락, 양쪽 컬럼/집계 기준 차이, 조인/필터 불일치, 성능상 주의점을 검토한다. "
                + "한국어로 작성하고, 첫 줄은 PASS 또는 WARN 또는 FAIL 중 하나로 시작한다. "
                + "운영자가 바로 수정할 수 있게 5개 이하 bullet로 근거와 수정 제안을 제시한다.";
        String review = openAiService.generate(instructions, prompt);
        comparison.setAiSqlReviewSummary(limit(review, 3900));
        comparison.setAiSqlReviewStatus(resolveReviewStatus(review));
        if ("FAIL".equals(comparison.getAiSqlReviewStatus())) {
            throw new IllegalArgumentException("AI SQL 사전 검토 결과: 저장 차단\n\n" + removeLeadingReviewStatus(review));
        }
    }

    private void enrichAiAnalysisIfNeeded(QueryComparisonDto comparison, QueryComparisonResultDto result) {
        if (!"Y".equalsIgnoreCase(comparison.getAiAnalysisEnabledYn())) {
            result.setAiAnalysisStatus("SKIPPED");
            return;
        }
        if (!"FAIL".equalsIgnoreCase(result.getResultStatus()) && !"ERROR".equalsIgnoreCase(result.getResultStatus())) {
            result.setAiAnalysisStatus("SKIPPED");
            return;
        }
        String batchContext = buildOracleBatchContext(result);
        result.setOracleBatchContext(batchContext);
        if (!openAiService.isConfigured()) {
            result.setAiAnalysisStatus("SKIPPED");
            result.setAiAnalysis("OpenAI API 설정이 없어 AI 분석을 실행하지 않았습니다. OPENAI_API_KEY를 설정하세요.");
            return;
        }
        try {
            String analysis = openAiService.generate(aiAnalysisInstructions(),
                    buildAiAnalysisInput(comparison, result, batchContext));
            result.setAiAnalysisStatus("SUCCESS");
            result.setAiAnalysis(limit(analysis, 3900));
            result.setAiActionGuide(extractActionGuide(analysis));
        } catch (Exception e) {
            result.setAiAnalysisStatus("ERROR");
            result.setAiAnalysis("AI 분석 실패: " + rootMessage(e));
        }
    }

    private String buildOracleBatchContext(QueryComparisonResultDto result) {
        SearchConditionDto condition = new SearchConditionDto();
        condition.setSize(5);
        LocalDateTime base = result.getRequestedAt() != null
                ? result.getRequestedAt()
                : LocalDateTime.now(queryCompareProperties.getSchedulerZoneId());
        condition.setStartDateTime(base.minusHours(6));
        LocalDateTime end = result.getFinishedAt() != null
                ? result.getFinishedAt()
                : LocalDateTime.now(queryCompareProperties.getSchedulerZoneId());
        condition.setEndDateTime(end.plusHours(1));
        condition.normalizePaging();
        List<JobExecutionDto> failedJobs = jobExecutionMapper.findFailedJobExecutions(condition);
        if (failedJobs == null || failedJobs.isEmpty()) {
            return "동일 시간대 Oracle 배치 실패 이력이 조회되지 않았습니다.";
        }
        StringBuilder builder = new StringBuilder();
        for (JobExecutionDto job : failedJobs) {
            builder.append("JOB_EXECUTION_ID=").append(job.getJobExecutionId())
                    .append(", JOB_NAME=").append(job.getJobName())
                    .append(", STATUS=").append(job.getStatus())
                    .append(", EXIT_CODE=").append(job.getExitCode())
                    .append(", START_TIME=").append(DateTimeUtils.format(job.getStartTime()))
                    .append(", END_TIME=").append(DateTimeUtils.format(job.getEndTime()))
                    .append(", EXIT_MESSAGE=").append(DateTimeUtils.summarize(job.getExitMessage(), 300))
                    .append('\n');
        }
        return limit(builder.toString(), 3900);
    }

    private String buildAiAnalysisInput(QueryComparisonDto comparison,
                                        QueryComparisonResultDto result,
                                        String batchContext) {
        return "비교명: " + comparison.getComparisonName()
                + "\n설명: " + nullSafe(comparison.getDescription())
                + "\n결과 상태: " + result.getResultStatus()
                + "\nSybase row count: " + result.getSybaseRowCount()
                + "\nOracle row count: " + result.getOracleRowCount()
                + "\nSybase hash: " + nullSafe(result.getSybaseResultHash())
                + "\nOracle hash: " + nullSafe(result.getOracleResultHash())
                + "\n불일치 요약: " + nullSafe(result.getMismatchSummary())
                + "\n오류 메시지: " + nullSafe(result.getErrorMessage())
                + "\n\n[Sybase SQL]\n" + comparison.getSybaseQuery()
                + "\n\n[Oracle SQL]\n" + comparison.getOracleQuery()
                + "\n\n[Oracle 배치 상태/실패 이력]\n" + nullSafe(batchContext);
    }

    private String aiAnalysisInstructions() {
        return "너는 이기종 DB 데이터 정합성 장애를 분석하는 운영 지원 AI다. "
                + "입력된 SQL, row count/hash 차이, 불일치 요약, Oracle 배치 실패 이력을 근거로 원인 후보와 점검 순서를 제시한다. "
                + "추측은 '가능성'으로 표시하고, 확정처럼 말하지 않는다. "
                + "한국어로 다음 제목을 반드시 포함한다: 원인 후보, 점검 순서, 조치 가이드. "
                + "운영자가 바로 수행할 수 있는 구체적 확인 항목 위주로 간결하게 작성한다.";
    }

    private String resolveReviewStatus(String review) {
        if (review == null) {
            return "WARN";
        }
        String normalized = review.trim().toUpperCase();
        if (normalized.startsWith("PASS")) {
            return "PASS";
        }
        if (normalized.startsWith("FAIL")) {
            return "FAIL";
        }
        return "WARN";
    }

    private String removeLeadingReviewStatus(String review) {
        if (review == null) {
            return null;
        }
        return review.replaceFirst("(?is)^\\s*(PASS|WARN|FAIL)\\s*[:\\-]?\\s*", "");
    }

    private String extractActionGuide(String analysis) {
        if (analysis == null) {
            return null;
        }
        int index = analysis.indexOf("조치 가이드");
        if (index < 0) {
            return limit(analysis, 3900);
        }
        return limit(analysis.substring(index), 3900);
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }

    private void notifyIfNeeded(QueryComparisonDto comparison, QueryComparisonResultDto result) {
        if (!"Y".equalsIgnoreCase(comparison.getNotifyEnabledYn())) {
            return;
        }
        boolean success = "SUCCESS".equalsIgnoreCase(result.getResultStatus());
        if (success && !"Y".equalsIgnoreCase(comparison.getNotifyOnSuccessYn())) {
            return;
        }
        if (success || "FAIL".equalsIgnoreCase(result.getResultStatus()) || "ERROR".equalsIgnoreCase(result.getResultStatus())) {
            validationNotificationService.notifyResult(
                    "PAIR",
                    comparison.getComparisonId(),
                    result.getResultId(),
                    result.getResultStatus(),
                    comparison.getComparisonName(),
                    result.getMessageSummary(),
                    comparison.getNotifyRecipients()
            );
        }
    }

    private static class ObjectProviderFallback implements java.util.function.Supplier<JdbcTemplate> {
        private final JdbcTemplate jdbcTemplate;

        ObjectProviderFallback(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Override
        public JdbcTemplate get() {
            return jdbcTemplate;
        }
    }
}

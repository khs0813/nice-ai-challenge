package com.example.batchmonitor.service;

import com.example.batchmonitor.dto.PageResult;
import com.example.batchmonitor.dto.QueryExpectationDto;
import com.example.batchmonitor.dto.QueryExpectationResultDto;
import com.example.batchmonitor.dto.QueryExpectationSearchConditionDto;
import com.example.batchmonitor.mapper.QueryExpectationMapper;
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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class QueryExpectationService {

    private static final int MAX_RESULT_ROWS = 10000;

    private final QueryExpectationMapper queryExpectationMapper;
    private final JdbcTemplate sybaseJdbcTemplate;
    private final JdbcTemplate oracleJdbcTemplate;
    private final ValidationNotificationService validationNotificationService;
    private final OpenAiService openAiService;

    public QueryExpectationService(QueryExpectationMapper queryExpectationMapper,
                                   DataSource dataSource,
                                   @Qualifier("sybaseQueryJdbcTemplate") ObjectProvider<JdbcTemplate> sybaseJdbcTemplateProvider,
                                   @Qualifier("oracleQueryJdbcTemplate") ObjectProvider<JdbcTemplate> oracleJdbcTemplateProvider,
                                   ValidationNotificationService validationNotificationService,
                                   OpenAiService openAiService) {
        this.queryExpectationMapper = queryExpectationMapper;
        JdbcTemplate defaultJdbcTemplate = new JdbcTemplate(dataSource);
        this.sybaseJdbcTemplate = sybaseJdbcTemplateProvider.getIfAvailable(new ObjectProviderFallback(defaultJdbcTemplate));
        this.oracleJdbcTemplate = oracleJdbcTemplateProvider.getIfAvailable(new ObjectProviderFallback(defaultJdbcTemplate));
        this.validationNotificationService = validationNotificationService;
        this.openAiService = openAiService;
    }

    public PageResult<QueryExpectationDto> findExpectations(QueryExpectationSearchConditionDto condition) {
        prepare(condition);
        return new PageResult<QueryExpectationDto>(
                queryExpectationMapper.findExpectations(condition),
                queryExpectationMapper.countExpectations(condition),
                condition.getPage(),
                condition.getSize()
        );
    }

    public QueryExpectationDto findExpectationById(Long expectationId) {
        QueryExpectationDto expectation = queryExpectationMapper.findExpectationById(expectationId);
        if (expectation == null) {
            throw new IllegalArgumentException("기대값 검증 설정을 찾을 수 없습니다. 검증 번호=" + expectationId);
        }
        return expectation;
    }

    @Transactional
    public void saveExpectation(QueryExpectationDto expectation, String username) {
        validateExpectation(expectation);
        validateQueryOnDatabase(jdbcTemplateFor(expectation.getTargetDbType()), expectation.getQueryText(), expectation.getTargetDbType());
        reviewSqlWithAiIfEnabled(expectation);
        if (expectation.getExpectationId() == null) {
            expectation.setCreatedBy(username);
            expectation.setUpdatedBy(username);
            queryExpectationMapper.insertExpectation(expectation);
            return;
        }
        expectation.setUpdatedBy(username);
        queryExpectationMapper.updateExpectation(expectation);
    }

    @Transactional
    public void deleteExpectation(Long expectationId) {
        queryExpectationMapper.deleteExpectation(expectationId);
    }

    @Transactional
    public Long requestExpectation(Long expectationId, String username) {
        QueryExpectationDto expectation = findExpectationById(expectationId);
        if (!"Y".equalsIgnoreCase(expectation.getEnabledYn())) {
            throw new IllegalArgumentException("중지된 기대값 검증은 실행할 수 없습니다.");
        }

        QueryExpectationResultDto result = new QueryExpectationResultDto();
        result.setExpectationId(expectationId);
        result.setRequestedBy(username);
        result.setRequestedAt(LocalDateTime.now());
        result.setStartedAt(LocalDateTime.now());
        result.setExpectedValue(expectation.getExpectedValue());

        try {
            validateExecutableSelect(expectation.getQueryText(), expectation.getTargetDbType());
            List<Map<String, Object>> rows = jdbcTemplateFor(expectation.getTargetDbType())
                    .queryForList(normalizeSql(expectation.getQueryText()));
            result.setActualRowCount((long) rows.size());
            if (rows.size() > MAX_RESULT_ROWS) {
                result.setResultStatus("ERROR");
                result.setErrorMessage("검증 가능한 최대 행 수는 " + MAX_RESULT_ROWS + "건입니다. actual=" + rows.size());
            } else {
                String canonical = canonicalize(rows);
                result.setActualResultHash(sha256(canonical));
                String actualValue = extractActualValue(expectation, rows, canonical, result.getActualResultHash());
                result.setActualValue(actualValue);
                if (matches(actualValue, expectation.getExpectedValue(), expectation.getExpectedOperator())) {
                    result.setResultStatus("SUCCESS");
                    result.setMismatchSummary("실제값이 기대값과 일치합니다.");
                } else {
                    result.setResultStatus("FAIL");
                    result.setMismatchSummary("기대값 [" + expectation.getExpectedValue() + "] "
                            + operatorLabel(expectation.getExpectedOperator()) + ", 실제값 [" + summarize(actualValue) + "]");
                }
            }
        } catch (Exception e) {
            result.setResultStatus("ERROR");
            result.setErrorMessage(e.getMessage());
        }

        result.setFinishedAt(LocalDateTime.now());
        queryExpectationMapper.insertResult(result);
        notifyIfNeeded(expectation, result);
        return result.getResultId();
    }

    public QueryExpectationResultDto findResultById(Long resultId) {
        return findResultById(resultId, false);
    }

    public QueryExpectationResultDto findResultDetail(Long resultId) {
        return findResultById(resultId, true);
    }

    private QueryExpectationResultDto findResultById(Long resultId, boolean required) {
        if (resultId == null) {
            if (required) {
                throw new IllegalArgumentException("기대값 검증 결과 번호가 필요합니다.");
            }
            return null;
        }
        QueryExpectationResultDto result = queryExpectationMapper.findResultById(resultId);
        if (result == null && required) {
            throw new IllegalArgumentException("기대값 검증 결과를 찾을 수 없습니다. 결과 번호=" + resultId);
        }
        return result;
    }

    public QueryExpectationResultDto findLatestResultByExpectationId(Long expectationId) {
        if (expectationId == null) {
            return null;
        }
        return queryExpectationMapper.findLatestResultByExpectationId(expectationId);
    }

    public List<QueryExpectationDto> findScheduledExpectations() {
        return queryExpectationMapper.findScheduledExpectations();
    }

    public boolean isScheduleDue(QueryExpectationDto expectation, LocalDateTime now) {
        if (expectation == null || !"Y".equalsIgnoreCase(expectation.getScheduleEnabledYn())) {
            return false;
        }
        if (expectation.getCronExpression() == null) {
            return false;
        }
        QueryExpectationResultDto latestResult = findLatestResultByExpectationId(expectation.getExpectationId());
        CronSequenceGenerator cron = new CronSequenceGenerator(expectation.getCronExpression());
        LocalDateTime baseTime = latestResult != null && latestResult.getRequestedAt() != null
                ? latestResult.getRequestedAt()
                : expectation.getUpdatedAt() != null ? expectation.getUpdatedAt() : expectation.getCreatedAt();
        if (baseTime == null) {
            baseTime = now;
        }
        Date base = Date.from(baseTime.atZone(ZoneId.systemDefault()).toInstant());
        Date next = cron.next(base);
        Date current = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        return !next.after(current);
    }

    public PageResult<QueryExpectationResultDto> findResults(QueryExpectationSearchConditionDto condition) {
        prepare(condition);
        return new PageResult<QueryExpectationResultDto>(
                queryExpectationMapper.findResults(condition),
                queryExpectationMapper.countResults(condition),
                condition.getPage(),
                condition.getSize()
        );
    }

    private void prepare(QueryExpectationSearchConditionDto condition) {
        condition.normalizePaging();
        condition.setStartDateTime(DateTimeUtils.parseStartDate(condition.getStartDate()));
        condition.setEndDateTime(DateTimeUtils.parseEndDateExclusive(condition.getEndDate()));
    }

    private void validateExpectation(QueryExpectationDto expectation) {
        if (expectation.getExpectationName() == null) {
            throw new IllegalArgumentException("검증명은 필수입니다.");
        }
        if (!"SYBASE".equals(expectation.getTargetDbType()) && !"ORACLE".equals(expectation.getTargetDbType())) {
            throw new IllegalArgumentException("대상 데이터베이스는 Sybase 또는 Oracle만 선택할 수 있습니다.");
        }
        validateExecutableSelect(expectation.getQueryText(), expectation.getTargetDbType());
        if (!isSupportedValueType(expectation.getExpectedValueType())) {
            throw new IllegalArgumentException("지원하지 않는 기대값 타입입니다.");
        }
        if (!isSupportedOperator(expectation.getExpectedOperator())) {
            throw new IllegalArgumentException("지원하지 않는 비교 연산자입니다.");
        }
        if (expectation.getExpectedValue() == null) {
            throw new IllegalArgumentException("기대값은 필수입니다.");
        }
        if ("Y".equalsIgnoreCase(expectation.getScheduleEnabledYn())) {
            if (expectation.getCronExpression() == null) {
                throw new IllegalArgumentException("스케줄을 사용하려면 자동 실행 주기가 필요합니다.");
            }
            try {
                new CronSequenceGenerator(expectation.getCronExpression());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("자동 실행 주기 형식이 올바르지 않습니다. 예: 0 0 7 * * *", e);
            }
        }
    }

    private boolean isSupportedValueType(String valueType) {
        return "SCALAR".equals(valueType) || "ROW_COUNT".equals(valueType)
                || "JSON".equals(valueType) || "HASH".equals(valueType);
    }

    private boolean isSupportedOperator(String operator) {
        return "EQ".equals(operator) || "NE".equals(operator) || "GT".equals(operator)
                || "GTE".equals(operator) || "LT".equals(operator) || "LTE".equals(operator)
                || "CONTAINS".equals(operator);
    }

    private JdbcTemplate jdbcTemplateFor(String targetDbType) {
        return "SYBASE".equalsIgnoreCase(targetDbType) ? sybaseJdbcTemplate : oracleJdbcTemplate;
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

    private String extractActualValue(QueryExpectationDto expectation, List<Map<String, Object>> rows,
                                      String canonical, String hash) {
        if ("ROW_COUNT".equals(expectation.getExpectedValueType())) {
            return String.valueOf(rows.size());
        }
        if ("JSON".equals(expectation.getExpectedValueType())) {
            return canonical;
        }
        if ("HASH".equals(expectation.getExpectedValueType())) {
            return hash;
        }
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> firstRow = rows.get(0);
        if (firstRow.isEmpty()) {
            return null;
        }
        return String.valueOf(firstRow.values().iterator().next());
    }

    private boolean matches(String actual, String expected, String operator) {
        if ("CONTAINS".equals(operator)) {
            return actual != null && expected != null && actual.contains(expected);
        }
        if ("EQ".equals(operator)) {
            return normalized(actual).equals(normalized(expected));
        }
        if ("NE".equals(operator)) {
            return !normalized(actual).equals(normalized(expected));
        }
        BigDecimal actualNumber = new BigDecimal(normalized(actual));
        BigDecimal expectedNumber = new BigDecimal(normalized(expected));
        int compared = actualNumber.compareTo(expectedNumber);
        if ("GT".equals(operator)) {
            return compared > 0;
        }
        if ("GTE".equals(operator)) {
            return compared >= 0;
        }
        if ("LT".equals(operator)) {
            return compared < 0;
        }
        if ("LTE".equals(operator)) {
            return compared <= 0;
        }
        return false;
    }

    private String normalized(String value) {
        return value == null ? "" : value.trim();
    }

    private String operatorLabel(String operator) {
        if ("EQ".equalsIgnoreCase(operator)) {
            return "같음";
        }
        if ("NE".equalsIgnoreCase(operator)) {
            return "다름";
        }
        if ("GT".equalsIgnoreCase(operator)) {
            return "초과";
        }
        if ("GTE".equalsIgnoreCase(operator)) {
            return "이상";
        }
        if ("LT".equalsIgnoreCase(operator)) {
            return "미만";
        }
        if ("LTE".equalsIgnoreCase(operator)) {
            return "이하";
        }
        if ("CONTAINS".equalsIgnoreCase(operator)) {
            return "포함";
        }
        return operator;
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

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage();
    }

    private String summarize(String value) {
        return DateTimeUtils.summarize(value, 180);
    }

    private void reviewSqlWithAiIfEnabled(QueryExpectationDto expectation) {
        if (!"Y".equalsIgnoreCase(expectation.getAiSqlReviewYn())) {
            expectation.setAiSqlReviewStatus(null);
            expectation.setAiSqlReviewSummary(null);
            return;
        }
        String prompt = "검증명: " + expectation.getExpectationName()
                + "\n설명: " + nullSafe(expectation.getDescription())
                + "\n대상 DB: " + expectation.getTargetDbType()
                + "\n기대값 타입: " + expectation.getExpectedValueType()
                + "\n비교 연산자: " + expectation.getExpectedOperator()
                + "\n기대값: " + nullSafe(expectation.getExpectedValue())
                + "\n\n[검증 SQL]\n" + expectation.getQueryText();
        String instructions = "너는 금융/정산 데이터 기대값 검증 SQL 리뷰어다. "
                + "단일 SQL이 등록된 기대값 타입과 비교 연산자에 맞게 검증 가능한지 점검한다. "
                + "위험 DML/DDL, 날짜/상태 조건 누락, 기대값 타입과 SELECT 결과 형태 불일치, 집계 기준 누락, NULL/공백/대소문자 처리 위험을 검토한다. "
                + "한국어로 작성하고, 첫 줄은 PASS 또는 WARN 또는 FAIL 중 하나로 시작한다. "
                + "운영자가 바로 수정할 수 있게 5개 이하 bullet로 근거와 수정 제안을 제시한다.";
        String review = openAiService.generate(instructions, prompt);
        expectation.setAiSqlReviewSummary(limit(review, 3900));
        expectation.setAiSqlReviewStatus(resolveReviewStatus(review));
        if ("FAIL".equals(expectation.getAiSqlReviewStatus())) {
            throw new IllegalArgumentException("AI SQL 사전 검토 결과: 저장 차단\n\n" + removeLeadingReviewStatus(review));
        }
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

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }

    private void notifyIfNeeded(QueryExpectationDto expectation, QueryExpectationResultDto result) {
        if (!"Y".equalsIgnoreCase(expectation.getNotifyEnabledYn())) {
            return;
        }
        boolean success = "SUCCESS".equalsIgnoreCase(result.getResultStatus());
        if (success && !"Y".equalsIgnoreCase(expectation.getNotifyOnSuccessYn())) {
            return;
        }
        if (success || "FAIL".equalsIgnoreCase(result.getResultStatus()) || "ERROR".equalsIgnoreCase(result.getResultStatus())) {
            validationNotificationService.notifyResult(
                    "SINGLE",
                    expectation.getExpectationId(),
                    result.getResultId(),
                    result.getResultStatus(),
                    expectation.getExpectationName(),
                    result.getMessageSummary(),
                    expectation.getNotifyRecipients()
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

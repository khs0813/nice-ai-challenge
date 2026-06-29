package com.example.batchmonitor.util;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DashboardMessageFormatter {

    private static final Pattern ROW_COUNT_PATTERN = Pattern.compile(
            "(?i).*row count differs\\s*:\\s*Sybase\\s*=\\s*(\\d+)\\s*,\\s*Oracle\\s*=\\s*(\\d+).*"
    );
    private static final Pattern TIMEOUT_PATTERN = Pattern.compile("(?i).*timed out after\\s+(\\d+)\\s+seconds.*");
    private static final Pattern EXPECTED_PATTERN = Pattern.compile("(?i).*expected\\s*\\[?([^\\],]+)\\]?\\s*,?\\s*EQ\\s*,?\\s*actual\\s*\\[?([^\\],]+)\\]?.*");

    private DashboardMessageFormatter() {
    }

    public static String formatMismatchSummary(Long sybaseRowCount, Long oracleRowCount, String rawMessage) {
        if (sybaseRowCount != null && oracleRowCount != null && !sybaseRowCount.equals(oracleRowCount)) {
            return rowCountMessage(sybaseRowCount, oracleRowCount);
        }
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            return "최근 검증에서 정합성 확인이 필요한 결과가 발견되었습니다.";
        }
        Matcher matcher = ROW_COUNT_PATTERN.matcher(rawMessage.trim());
        if (matcher.matches()) {
            return rowCountMessage(Long.valueOf(matcher.group(1)), Long.valueOf(matcher.group(2)));
        }
        return formatErrorMessageForUser(rawMessage);
    }

    public static String formatSqlValidationMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            return "SQL 실행 전 확인이 필요한 경고가 발견되었습니다.";
        }
        String normalized = rawMessage.replaceFirst("(?is)^\\s*(PASS|WARN|FAIL|WARNING|ERROR)\\s*[:\\-]?\\s*", "").trim();
        if (containsIgnoreCase(normalized, "SQL warning detected")) {
            return "SQL 실행 전 확인이 필요한 경고가 발견되었습니다.";
        }
        if (containsIgnoreCase(normalized, "date") || normalized.contains("날짜")) {
            return "날짜 조건 누락 시 대량 조회 가능성이 있어 실행 전 확인이 필요합니다.";
        }
        if (containsIgnoreCase(normalized, "status") || normalized.contains("상태")) {
            return "상태 조건 또는 처리 기준 컬럼을 확인해야 합니다.";
        }
        if (containsIgnoreCase(normalized, "column") || normalized.contains("컬럼")) {
            return "비교 기준 컬럼과 결과 컬럼 구성이 일치하는지 점검해야 합니다.";
        }
        return DateTimeUtils.summarize(normalized, 100);
    }

    public static String formatErrorMessageForUser(String rawMessage) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            return "-";
        }
        String message = rawMessage.trim();
        Matcher timeoutMatcher = TIMEOUT_PATTERN.matcher(message);
        if (timeoutMatcher.matches()) {
            return "쿼리 실행 시간이 " + timeoutMatcher.group(1) + "초를 초과하여 검증이 중단되었습니다.";
        }
        Matcher rowCountMatcher = ROW_COUNT_PATTERN.matcher(message);
        if (rowCountMatcher.matches()) {
            return rowCountMessage(Long.valueOf(rowCountMatcher.group(1)), Long.valueOf(rowCountMatcher.group(2)));
        }
        if (containsIgnoreCase(message, "SQL warning detected")) {
            return "SQL 실행 전 확인이 필요한 경고가 발견되었습니다.";
        }
        if (containsIgnoreCase(message, "ORA-00942")) {
            return "검증 대상 테이블 또는 뷰를 찾을 수 없습니다. 테이블명과 조회 권한을 확인하세요.";
        }
        Matcher expectedMatcher = EXPECTED_PATTERN.matcher(message);
        if (expectedMatcher.matches()) {
            return "기대값 " + expectedMatcher.group(1).trim() + "과 실제값 "
                    + expectedMatcher.group(2).trim() + "이 일치하지 않습니다.";
        }
        if (containsIgnoreCase(message, "connection")) {
            return "DB 연결 상태를 확인해야 합니다. 연결 정보 또는 네트워크 상태를 점검하세요.";
        }
        if (containsIgnoreCase(message, "permission") || containsIgnoreCase(message, "denied")) {
            return "검증에 필요한 DB 권한이 부족합니다. 조회 권한을 확인하세요.";
        }
        return DateTimeUtils.summarize(message, 120);
    }

    public static String formatSeverityLabel(String severity) {
        if (severity == null || severity.trim().isEmpty()) {
            return "LOW";
        }
        String normalized = severity.trim().toUpperCase();
        if ("HIGH".equals(normalized) || "MEDIUM".equals(normalized) || "LOW".equals(normalized)) {
            return normalized;
        }
        return "LOW";
    }

    public static String formatAiStatusLabel(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "대기";
        }
        String normalized = status.trim().toUpperCase();
        if ("SUCCESS".equals(normalized)) {
            return "완료";
        }
        if ("ERROR".equals(normalized) || "FAIL".equals(normalized)) {
            return "실패";
        }
        if ("SKIPPED".equals(normalized) || "REQUESTED".equals(normalized)) {
            return "대기";
        }
        if ("RUNNING".equals(normalized)) {
            return "진행 중";
        }
        return status;
    }

    private static String rowCountMessage(Long sybaseRowCount, Long oracleRowCount) {
        long diff = Math.abs(sybaseRowCount.longValue() - oracleRowCount.longValue());
        if (sybaseRowCount.longValue() >= oracleRowCount.longValue()) {
            return "Sybase 기준 " + formatNumber(sybaseRowCount) + "건 중 Oracle에는 "
                    + formatNumber(oracleRowCount) + "건만 확인되어 " + formatNumber(diff)
                    + "건의 누락 가능성이 있습니다.";
        }
        return "Oracle 대상 데이터가 Sybase 원천 데이터보다 " + formatNumber(diff)
                + "건 많습니다. 중복 반영 또는 기준 조건 차이를 확인해야 합니다.";
    }

    private static String formatNumber(Long value) {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(value);
    }

    private static String formatNumber(long value) {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(value);
    }

    private static boolean containsIgnoreCase(String source, String target) {
        return source != null && target != null && source.toLowerCase(Locale.ROOT).contains(target.toLowerCase(Locale.ROOT));
    }
}

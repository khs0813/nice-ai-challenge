package com.example.batchmonitor.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateTimeUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtils() {
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static String durationText(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            return "-";
        }
        LocalDateTime effectiveEndTime = endTime != null ? endTime : LocalDateTime.now();
        Duration duration = Duration.between(startTime, effectiveEndTime);
        if (duration.isNegative()) {
            return "-";
        }
        return durationText(duration);
    }

    public static String durationText(Duration duration) {
        if (duration == null || duration.isNegative()) {
            return "-";
        }
        long seconds = duration.getSeconds();
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainSeconds = seconds % 60;

        if (days > 0) {
            return String.format("%d일 %02d:%02d:%02d", days, hours, minutes, remainSeconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes, remainSeconds);
    }

    public static String summarize(String text, int maxLength) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxLength)) + "...";
    }

    public static LocalDateTime parseStartDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim()).atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDateTime parseEndDateExclusive(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim()).plusDays(1).atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

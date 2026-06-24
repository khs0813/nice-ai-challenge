package com.example.batchmonitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
@ConfigurationProperties(prefix = "query.compare")
public class QueryCompareProperties {

    private final DbProperties sybase = new DbProperties();
    private final DbProperties oracle = new DbProperties();
    private final SchedulerProperties scheduler = new SchedulerProperties();

    public DbProperties getSybase() {
        return sybase;
    }

    public DbProperties getOracle() {
        return oracle;
    }

    public SchedulerProperties getScheduler() {
        return scheduler;
    }

    public ZoneId getSchedulerZoneId() {
        return ZoneId.of(scheduler.getTimeZone().trim());
    }

    public static class DbProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }
    }

    public static class SchedulerProperties {
        private String timeZone = "Asia/Seoul";

        public String getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(String timeZone) {
            if (timeZone == null || timeZone.trim().isEmpty()) {
                this.timeZone = "Asia/Seoul";
                return;
            }
            this.timeZone = timeZone.trim();
        }
    }
}

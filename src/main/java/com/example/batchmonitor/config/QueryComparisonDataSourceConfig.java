package com.example.batchmonitor.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class QueryComparisonDataSourceConfig {

    @Bean
    @ConditionalOnProperty("query.compare.sybase.url")
    public JdbcTemplate sybaseQueryJdbcTemplate(QueryCompareProperties properties) {
        return new JdbcTemplate(buildDataSource(
                properties.getSybase().getUrl(),
                properties.getSybase().getUsername(),
                properties.getSybase().getPassword(),
                properties.getSybase().getDriverClassName()
        ));
    }

    @Bean
    @ConditionalOnProperty("query.compare.oracle.url")
    public JdbcTemplate oracleQueryJdbcTemplate(QueryCompareProperties properties) {
        return new JdbcTemplate(buildDataSource(
                properties.getOracle().getUrl(),
                properties.getOracle().getUsername(),
                properties.getOracle().getPassword(),
                properties.getOracle().getDriverClassName()
        ));
    }

    private DataSource buildDataSource(String url, String username, String password, String driverClassName) {
        DataSourceBuilder<?> builder = DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password);
        if (driverClassName != null && !driverClassName.trim().isEmpty()) {
            builder.driverClassName(driverClassName);
        }
        return builder.build();
    }
}

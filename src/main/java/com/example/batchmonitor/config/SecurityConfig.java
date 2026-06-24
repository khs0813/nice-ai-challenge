package com.example.batchmonitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${monitor.security.admin.username}")
    private String adminUsername;

    @Value("${monitor.security.admin.password-hash}")
    private String adminPasswordHash;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        if (isBlank(adminUsername) || isBlank(adminPasswordHash)) {
            throw new IllegalStateException(
                    "관리자 계정 설정이 필요합니다. 운영 실행은 MONITOR_ADMIN_USERNAME, " +
                            "MONITOR_ADMIN_PASSWORD_HASH 환경변수를 지정하고, 로컬 개발 실행은 " +
                            "SPRING_PROFILES_ACTIVE=oracle,dev 프로필을 사용하세요."
            );
        }
        auth.inMemoryAuthentication()
                .passwordEncoder(passwordEncoder())
                .withUser(adminUsername)
                .password(adminPasswordHash)
                .roles("ADMIN");
    }

    @Override
    public void configure(WebSecurity web) {
        web.requestRejectedHandler(this::handleRejectedRequest);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers()
                    .frameOptions().deny()
                    .contentSecurityPolicy("default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'")
                    .and()
                    .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
                    .and()
                    .and()
                .authorizeRequests()
                    .antMatchers(
                            "/login",
                            "/error/**",
                            "/css/**",
                            "/js/**",
                            "/images/**",
                            "/webjars/**",
                            "/favicon.ico"
                    ).permitAll()
                    .anyRequest().authenticated()
                    .and()
                .formLogin()
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/dashboard", true)
                    .failureUrl("/login?error")
                    .permitAll()
                    .and()
                .logout()
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .and()
                .exceptionHandling()
                    .accessDeniedPage("/error/access-denied");
    }

    private void handleRejectedRequest(HttpServletRequest request,
                                       HttpServletResponse response,
                                       RequestRejectedException exception) throws IOException {
        if (!response.isCommitted()) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

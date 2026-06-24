# Batch Monitor

Spring Batch 메타데이터 테이블을 조회 전용으로 모니터링하는 Java 8 + Spring Boot 2.x 기반 관리자 웹 애플리케이션입니다.

- 로그인 / 로그아웃
- 대시보드 요약 카드
- Job 실행 이력 검색 및 페이징
- Job 상세, Job Parameter, Step 실행 목록
- Step 상세 및 Step 통계 카드
- 실패 Job 모니터링
- 실행 중 Job 모니터링
- Sybase/Oracle 쿼리 비교 설정 등록 및 관리
- OpenAI API 기반 SQL 사전 검토와 불일치 원인 분석/조치 가이드
- 사용자 요청 쿼리 비교 결과 목록 조회
- NICE 기업 사이트 느낌을 참고한 네이비/블루 계열 기업형 UI

## 기술 스택

- Java 8
- Spring Boot 2.7.18
- Spring MVC
- Spring Security
- Thymeleaf
- MyBatis Spring Boot Starter 2.3.2
- Maven
- Oracle JDBC
- Oracle profile
- Jasypt Spring Boot Starter

## 실행 방법

### 로컬 테스트 DB로 실행

기본 프로필은 `test`입니다. 별도 DB 없이 H2 메모리 DB와 샘플 데이터로 실행됩니다.

```bash
mvn spring-boot:run
```

브라우저에서 접속합니다.

```text
http://localhost:8080/login
```

개발용 로그인은 `admin / admin1234`입니다.

테스트 프로필은 다음 파일을 사용합니다.

- `src/main/resources/application-test.yml`
- `src/main/resources/db/test-schema.sql`
- `src/main/resources/db/test-data.sql`

### Oracle DB로 실행

운영 또는 실제 Oracle DB 조회 시에는 `oracle` 프로필과 실제 DB 접속 정보, 관리자 계정 정보를 환경 변수로 지정한 뒤 실행합니다.

```bash
SPRING_PROFILES_ACTIVE=oracle \
ORACLE_JDBC_URL='jdbc:oracle:thin:@//db-host:1521/service_name' \
ORACLE_USERNAME='oracle_user' \
ORACLE_PASSWORD='ENC(encrypted_oracle_password)' \
MONITOR_ADMIN_USERNAME='admin' \
MONITOR_ADMIN_PASSWORD_HASH='ENC(encrypted_bcrypt_hash)' \
JASYPT_ENCRYPTOR_PASSWORD='jasypt-master-password' \
mvn spring-boot:run
```

브라우저에서 접속합니다.

```text
http://localhost:8080/login
```

IntelliJ에서 실행한다면 Environment variables에 `ORACLE_JDBC_URL`, `ORACLE_USERNAME`, `ORACLE_PASSWORD`, `MONITOR_ADMIN_USERNAME`, `MONITOR_ADMIN_PASSWORD_HASH`, `JASYPT_ENCRYPTOR_PASSWORD`를 추가하세요.

`ORACLE_PASSWORD`, `MONITOR_ADMIN_PASSWORD_HASH`는 Jasypt 암호문인 `ENC(...)` 형태로 둘 수 있습니다. `MONITOR_ADMIN_PASSWORD_HASH`의 원문은 평문 비밀번호가 아니라 Spring Security가 검증할 수 있는 BCrypt 해시입니다. `JASYPT_ENCRYPTOR_PASSWORD`는 복호화 키이므로 설정 파일에 저장하지 말고 운영 환경의 Secret Manager, CI/CD secret, 서버 환경변수 등으로만 주입하세요.

실제 Oracle DB를 사용하되 관리자 계정 환경변수 없이 로컬에서 실행해야 한다면 `dev` 프로필을 추가로 켭니다. 이 경우 개발용 로그인은 `admin / admin1234`입니다.

```bash
SPRING_PROFILES_ACTIVE=oracle,dev \
ORACLE_JDBC_URL='jdbc:oracle:thin:@//db-host:1521/service_name' \
ORACLE_USERNAME='oracle_user' \
ORACLE_PASSWORD='oracle_password' \
mvn spring-boot:run
```

IntelliJ Run Configuration에서 실행한다면 Environment variables에 다음처럼 넣으면 됩니다.

```text
SPRING_PROFILES_ACTIVE=oracle,dev
ORACLE_JDBC_URL=jdbc:oracle:thin:@//db-host:1521/service_name
ORACLE_USERNAME=oracle_user
ORACLE_PASSWORD=oracle_password
```

## Jasypt 설정

기본 암호화 설정은 `application.yml`에 있습니다.

```yaml
jasypt:
  encryptor:
    password: ${JASYPT_ENCRYPTOR_PASSWORD}
    algorithm: PBEWITHHMACSHA512ANDAES_256
    iv-generator-classname: org.jasypt.iv.RandomIvGenerator
```

암호문을 생성할 때는 운영과 동일한 `JASYPT_ENCRYPTOR_PASSWORD`와 알고리즘을 사용하세요. 생성된 값은 `ENC(...)`로 감싸서 환경변수나 외부 설정에 넣습니다.

## DB 연결 설정

기본 Oracle 설정 예시는 다음과 같습니다.

```yaml
spring:
  datasource:
    url: ${ORACLE_JDBC_URL}
    username: ${ORACLE_USERNAME}
    password: ${ORACLE_PASSWORD}
    driver-class-name: oracle.jdbc.OracleDriver
```

이 프로젝트는 배치 실행 기능을 포함하지 않습니다. 이미 존재하는 Spring Batch 메타데이터 테이블을 조회하는 것을 전제로 합니다.

## Spring Batch 메타데이터 테이블

조회 대상 테이블은 다음과 같습니다.

- `BATCH_JOB_INSTANCE`
- `BATCH_JOB_EXECUTION`
- `BATCH_JOB_EXECUTION_PARAMS`
- `BATCH_JOB_EXECUTION_CONTEXT`
- `BATCH_STEP_EXECUTION`
- `BATCH_STEP_EXECUTION_CONTEXT`

현재 화면 기능에서 주로 사용하는 테이블은 `BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_JOB_EXECUTION_PARAMS`, `BATCH_STEP_EXECUTION`입니다.

## Job Parameter 테이블 구조 선택

Spring Batch 버전에 따라 `BATCH_JOB_EXECUTION_PARAMS` 컬럼 구조가 다릅니다.

### legacy

Spring Batch 4 이하에서 자주 쓰이는 구조입니다.

- `TYPE_CD`
- `KEY_NAME`
- `STRING_VAL`
- `DATE_VAL`
- `LONG_VAL`
- `DOUBLE_VAL`
- `IDENTIFYING`

### modern

Spring Batch 5 이상 구조입니다.

- `PARAMETER_NAME`
- `PARAMETER_TYPE`
- `PARAMETER_VALUE`
- `IDENTIFYING`

설정은 `application.yml`에서 변경할 수 있습니다.

```yaml
batch:
  monitor:
    parameter-schema: legacy
```

Spring Batch 5 테이블을 조회하려면 다음처럼 변경하세요.

```yaml
batch:
  monitor:
    parameter-schema: modern
```

관련 SQL은 `src/main/resources/mapper/JobParameterMapper.xml`에 legacy/modern 쿼리를 분리해두었습니다.

## 주요 URL

| 기능 | URL |
| --- | --- |
| 로그인 | `GET /login` |
| 로그아웃 | `POST /logout` |
| 대시보드 | `GET /dashboard` |
| Job 실행 이력 | `GET /jobs` |
| Job 상세 | `GET /jobs/{jobExecutionId}` |
| 실패 Job | `GET /jobs/failed` |
| 실행 중 Job | `GET /jobs/running` |
| Step 상세 | `GET /steps/{stepExecutionId}` |
| Job Parameter | `GET /parameters` |
| 쿼리 비교 설정 | `GET /query-comparisons` |
| 쿼리 비교 결과 | `GET /query-comparison-results` |

## 쿼리 비교 모니터링 테이블

Sybase 쿼리와 Oracle 쿼리 한 쌍을 등록하고, 사용자가 요청한 비교 실행 결과를 조회하기 위한 테이블 DDL은 다음 파일에 있습니다.

```text
src/main/resources/db/query-comparison-oracle.sql
```

주요 테이블은 다음과 같습니다.

- `BM_QUERY_COMPARISON`: 비교명, Sybase 쿼리, Oracle 쿼리, 비교 방식, 사용 여부 저장
- `BM_QUERY_COMPARISON_RESULT`: 요청자, 요청/시작/종료 시각, 결과 상태, 양쪽 행 수, 결과 해시, 불일치 메시지 저장

비교 결과 상태는 `SUCCESS`, `FAIL`, `ERROR`를 주로 사용합니다. 쿼리 비교 목록에서 `요청`을 누르면 두 쿼리를 바로 실행하고, row count와 결과 해시를 비교해 결과 목록으로 이동합니다.

이미 `BM_QUERY_COMPARISON` 테이블을 만든 환경에 스케줄 컬럼만 추가하려면 다음 파일을 적용합니다.

```text
src/main/resources/db/query-comparison-schedule-oracle.sql
```

쿼리를 등록하거나 수정할 때 Sybase/Oracle 각각의 데이터베이스에서 SELECT 쿼리를 1건 제한으로 검증합니다. 검증에 실패하면 저장하지 않고 등록 화면에 오류를 표시합니다.

AI SQL 사전 검토를 켜면 저장 전 OpenAI API로 SQL의 검증 기준, 위험 문법, 날짜/상태 조건 누락, 컬럼/집계 기준 차이를 검토합니다. 이기종 DB 검증은 양쪽 SQL의 비교 기준을 함께 점검하고, 기대값 검증은 단일 SQL과 기대값 타입/연산자의 적합성을 점검합니다. FAIL 판정이면 저장하지 않습니다.

스케줄 실행은 Spring cron 표현식을 사용합니다.

```text
0 */10 * * * *  # 10분마다
0 0 2 * * *     # 매일 02:00
```

스케줄러는 기본적으로 60초마다 실행 대상 비교 ID를 확인합니다. 확인 주기는 `query.compare.scheduler.fixed-delay-ms`로 조정할 수 있습니다.
cron 기준 타임존은 기본 `Asia/Seoul`이며, `query.compare.scheduler.time-zone` 또는 `QUERY_COMPARE_SCHEDULER_TIME_ZONE` 환경변수로 변경할 수 있습니다.

테스트 프로필에서는 H2 테스트 DB에서 양쪽 쿼리를 실행합니다. 실제 Sybase/Oracle DB를 고정 연결하려면 애플리케이션 설정에 다음 값을 추가합니다.

```yaml
query:
  compare:
    sybase:
      url: jdbc:sybase:Tds:sybase-host:5000/db_name
      username: sybase_user
      password: sybase_password
      driver-class-name: com.sybase.jdbc4.jdbc.SybDriver
    oracle:
      url: jdbc:oracle:thin:@//oracle-host:1521/service_name
      username: oracle_user
      password: oracle_password
      driver-class-name: oracle.jdbc.OracleDriver
```

Sybase JDBC 드라이버는 Maven Central에 없는 경우가 많으므로 사내 Nexus/로컬 Maven 저장소에 등록한 뒤 `pom.xml`에 runtime dependency로 추가해야 합니다.

### OpenAI AI 분석 설정

AI SQL 검토와 불일치 원인 분석은 OpenAI Responses API를 사용합니다. 운영 환경에서는 API 키를 환경 변수로 주입하세요.

```bash
export OPENAI_API_KEY=sk-...
export OPENAI_MODEL=gpt-5.5
```

기본 설정은 `src/main/resources/application.yml`의 `openai.*` 항목에서 변경할 수 있습니다. 기존 Oracle 스키마에 AI 컬럼을 추가하려면 다음 마이그레이션을 적용합니다.

```text
src/main/resources/db/query-comparison-ai-oracle.sql
```

AI 분석은 쿼리 비교 결과가 `FAIL` 또는 `ERROR`일 때 실행되며, 검증 시간대의 Oracle 배치 실패 이력과 불일치 요약을 함께 전달해 원인 후보와 조치 가이드를 생성합니다.

## 패키지 구조

```text
src/main/java/com/example/batchmonitor
 ├── BatchMonitorApplication.java
 ├── config
 │    ├── SecurityConfig.java
 │    └── MyBatisConfig.java
 ├── controller
 │    ├── AuthController.java
 │    ├── DashboardController.java
 │    ├── JobExecutionController.java
 │    ├── StepExecutionController.java
 │    ├── JobParameterController.java
 │    ├── GlobalModelAttribute.java
 │    └── GlobalExceptionHandler.java
 ├── service
 │    ├── DashboardService.java
 │    ├── JobExecutionService.java
 │    ├── StepExecutionService.java
 │    └── JobParameterService.java
 ├── mapper
 │    ├── DashboardMapper.java
 │    ├── JobExecutionMapper.java
 │    ├── StepExecutionMapper.java
 │    └── JobParameterMapper.java
 ├── dto
 │    ├── DashboardSummaryDto.java
 │    ├── JobExecutionDto.java
 │    ├── JobExecutionDetailDto.java
 │    ├── StepExecutionDto.java
 │    ├── StepExecutionDetailDto.java
 │    ├── JobParameterDto.java
 │    ├── SearchConditionDto.java
 │    └── PageResult.java
 └── util
      └── DateTimeUtils.java
```

## 리소스 구조

```text
src/main/resources
 ├── application.yml
 ├── application-oracle.yml
 ├── mapper
 │    ├── DashboardMapper.xml
 │    ├── JobExecutionMapper.xml
 │    ├── StepExecutionMapper.xml
 │    └── JobParameterMapper.xml
 ├── templates
 │    ├── layout/base.html
 │    ├── auth/login.html
 │    ├── dashboard/index.html
 │    ├── job/list.html
 │    ├── job/detail.html
 │    ├── job/failed.html
 │    ├── job/running.html
 │    ├── step/detail.html
 │    ├── parameter/list.html
 │    └── error/custom-error.html
 └── static
      ├── css
      │    ├── common.css
      │    ├── layout.css
      │    ├── dashboard.css
      │    └── table.css
      └── js/common.js
```

## 보안 메모

- 로그인 폼에는 CSRF 토큰이 적용되어 있습니다.
- 모니터링 화면은 인증된 사용자만 접근 가능합니다.
- DB 데이터 수정 기능은 없습니다.
- MyBatis 파라미터 바인딩을 사용합니다.
- 기본 admin 계정은 개발용입니다.
- 운영 환경에서는 사용자 계정을 DB 기반으로 전환하세요.

## 향후 확장 아이디어

- 실패 Job 알림
- Job별 실패율 통계
- 일자별 실행 건수 차트
- Job별 평균 실행 시간 차트
- Step별 병목 분석
- 여러 DB 모니터링
- 사용자 권한 관리
- 배치 수동 재실행/중지 기능

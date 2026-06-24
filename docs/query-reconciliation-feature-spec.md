# Query Reconciliation Feature Specification

## 1. 목적

Spring Batch 메타데이터 모니터링 기능은 유지하면서, 운영 데이터 불일치로 인한 결제/정산 문제를 조기에 발견하는 신규 검증 기능을 추가한다.

신규 기능은 사용자가 직접 등록한 쿼리를 기준으로 다음 두 가지 업무를 수행한다.

- Sybase 쿼리 결과와 Oracle 쿼리 결과를 비교한다.
- 단일 DB 쿼리 결과가 사용자가 등록한 기대값과 일치하는지 검증한다.

각 검증 업무는 수동 실행과 cron 기반 자동 실행을 지원하고, 실행 결과를 화면에서 확인할 수 있어야 한다. 불일치 또는 오류 발생 시 사용자에게 알림을 제공한다.

## 2. 현 프로젝트 기준

현재 프로젝트는 Spring Boot 2.7.18, Java 8, Thymeleaf, MyBatis, Spring Security 기반이다.

이미 다음 쿼리 비교 관련 초안 구현이 존재한다.

- `QueryComparisonController`
- `QueryComparisonService`
- `QueryComparisonScheduler`
- `QueryComparisonMapper`
- `BM_QUERY_COMPARISON`
- `BM_QUERY_COMPARISON_RESULT`
- `/query-comparisons`
- `/query-comparison-results`

따라서 이번 개발은 기존 Spring Batch 모니터링 기능을 수정하지 않고, 위 쿼리 비교 기능을 확장/정리하는 방식으로 진행한다.

## 3. 범위

### 3.1 포함

- 좌측 메뉴에 신규 검증 메뉴 추가
- Sybase/Oracle 쿼리쌍 비교 업무 등록, 수정, 중지
- 단일쿼리 기대값 검증 업무 등록, 수정, 중지
- 업무별 cron 스케줄 등록 및 매일 특정 시간 자동 실행
- 수동 즉시 실행
- 실행 이력 조회
- 실행 결과 상세 조회
- 일치, 불일치, 오류 상태 표시
- 불일치 상세 요약 제공
- 알림 대상 및 알림 정책 정의
- 신규 DB 테이블 또는 기존 비교 테이블 확장
- SELECT/WITH 쿼리만 허용하는 실행 안전장치

### 3.2 제외

- 기존 Spring Batch 메타데이터 조회 로직 변경
- 기존 Batch Job 실행/중단 기능 추가
- 데이터 수정 쿼리 실행 기능
- 대량 결과 전체 다운로드 기능
- DB 계정/권한 관리 UI

## 4. 메뉴 구조

좌측 메뉴는 기존 Batch Monitor 메뉴를 유지하고, 신규 기능은 별도 그룹처럼 보이도록 추가한다.

권장 메뉴:

- Dashboard
- Job Executions
- Failed Jobs
- Running Jobs
- Job Parameters
- Data Validation
- Query Pair Rules
- Single Query Rules
- Validation Results
- Settings

기존 `Query Compare`, `Compare Results` 메뉴는 다음 중 하나로 정리한다.

- 기존 메뉴명을 `Query Pair Rules`, `Validation Results`로 변경한다.
- 또는 기존 메뉴를 유지하되 신규 `Single Query Rules` 메뉴만 추가한다.

## 5. 기능 요구사항

### 5.1 쿼리쌍 비교 업무

사용자는 하나의 업무단위로 Sybase 쿼리와 Oracle 쿼리를 등록한다.

입력 항목:

- 업무명
- 설명
- Sybase 쿼리
- Oracle 쿼리
- 비교 방식
- 사용 여부
- 스케줄 사용 여부
- cron 표현식
- 알림 사용 여부
- 알림 대상

비교 방식:

- `EXACT`: 컬럼명과 값이 정규화된 결과 전체가 동일해야 성공
- `HASH`: 정규화된 결과의 해시가 동일하면 성공

실행 결과:

- `SUCCESS`: 두 결과가 일치
- `FAIL`: 두 결과가 불일치
- `ERROR`: 쿼리 실행 실패, 타임아웃, 제한 초과 등

불일치 요약:

- row count 차이
- 첫 번째 불일치 row 위치
- Sybase/Oracle 값 요약
- 양쪽 결과 해시

### 5.2 단일쿼리 기대값 검증 업무

사용자는 단일 쿼리와 기대값을 등록한다.

입력 항목:

- 업무명
- 설명
- 대상 DB 타입: `SYBASE`, `ORACLE`
- 검증 쿼리
- 기대값 타입
- 기대값
- 비교 연산자
- 사용 여부
- 스케줄 사용 여부
- cron 표현식
- 알림 사용 여부
- 알림 대상

기대값 타입:

- `SCALAR`: 단일 컬럼/단일 row 값
- `ROW_COUNT`: 결과 row 수
- `JSON`: 결과를 정규화한 JSON 문자열
- `HASH`: 결과 정규화 해시

비교 연산자:

- `EQ`: 같음
- `NE`: 같지 않음
- `GT`: 큼
- `GTE`: 크거나 같음
- `LT`: 작음
- `LTE`: 작거나 같음
- `CONTAINS`: 문자열 포함

예시:

- "전일 미정산 건수는 0이어야 한다."
- 대상 DB: Oracle
- 쿼리: `SELECT COUNT(*) AS CNT FROM SETTLEMENT WHERE STATUS = 'PENDING' AND BIZ_DATE = TRUNC(SYSDATE) - 1`
- 기대값 타입: `SCALAR`
- 비교 연산자: `EQ`
- 기대값: `0`

### 5.3 스케줄 실행

사용자는 각 업무별로 cron 표현식을 등록할 수 있어야 한다.

요구사항:

- Spring cron 6필드 형식을 사용한다.
- 예: 매일 오전 7시 실행은 `0 0 7 * * *`
- 스케줄 사용 여부가 `Y`이고 업무 사용 여부가 `Y`인 업무만 실행한다.
- 동일 업무가 이미 실행 중이면 중복 실행하지 않는다.
- 마지막 실행시각 기준으로 다음 실행 대상인지 판단한다.
- 서버 재기동 후에도 DB 이력 기준으로 중복 실행을 최소화한다.

### 5.4 수동 실행

업무 목록에서 사용자가 "실행" 버튼을 누르면 즉시 검증을 수행한다.

요구사항:

- 비활성화된 업무는 실행할 수 없다.
- 실행 후 결과 상세 또는 결과 목록으로 이동한다.
- 실행자는 로그인 사용자명을 저장한다.

### 5.5 결과 조회

결과 목록 검색 조건:

- 업무명
- 업무 유형: `PAIR`, `SINGLE`
- 결과 상태
- 요청자
- 요청일 시작/종료

결과 상세 표시:

- 업무명
- 업무 유형
- 실행자
- 요청/시작/종료 시각
- 실행 소요시간
- 결과 상태
- row count
- 결과 hash
- 불일치 요약
- 오류 메시지
- 단일쿼리의 실제값/기대값/연산자

## 6. 알림 요구사항

최소 구현은 화면 알림 이력 저장으로 한다. 이후 이메일, Slack, Teams 등 외부 채널 확장이 가능하도록 인터페이스를 둔다.

알림 발생 조건:

- 결과 상태가 `FAIL`
- 결과 상태가 `ERROR`
- 선택 옵션으로 `SUCCESS`도 알림 가능

알림 내용:

- 업무명
- 업무 유형
- 결과 상태
- 실행시각
- 불일치/오류 요약
- 결과 상세 URL

알림 저장 테이블을 두고, 화면에서 읽음/미읽음 상태를 확인할 수 있게 한다.

## 7. 데이터베이스 설계안

기존 `BM_QUERY_COMPARISON`, `BM_QUERY_COMPARISON_RESULT`를 유지하면서 단일쿼리 기능을 별도 테이블로 추가하는 방식을 권장한다. 기존 구현 영향이 작고, 업무 유형별 컬럼 의미가 명확하다.

### 7.1 신규 테이블: 단일쿼리 검증 업무

```sql
CREATE TABLE BM_QUERY_EXPECTATION (
    EXPECTATION_ID          NUMBER(19)      NOT NULL,
    EXPECTATION_NAME        VARCHAR2(200)   NOT NULL,
    DESCRIPTION             VARCHAR2(1000),
    TARGET_DB_TYPE          VARCHAR2(20)    NOT NULL,
    QUERY_TEXT              CLOB            NOT NULL,
    EXPECTED_VALUE_TYPE     VARCHAR2(20)    NOT NULL,
    EXPECTED_OPERATOR       VARCHAR2(20)    NOT NULL,
    EXPECTED_VALUE          CLOB            NOT NULL,
    ENABLED_YN              CHAR(1)         DEFAULT 'Y' NOT NULL,
    SCHEDULE_ENABLED_YN     CHAR(1)         DEFAULT 'N' NOT NULL,
    CRON_EXPRESSION         VARCHAR2(100),
    NOTIFY_ENABLED_YN       CHAR(1)         DEFAULT 'Y' NOT NULL,
    NOTIFY_ON_SUCCESS_YN    CHAR(1)         DEFAULT 'N' NOT NULL,
    NOTIFY_RECIPIENTS       VARCHAR2(1000),
    CREATED_BY              VARCHAR2(100),
    CREATED_AT              TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_BY              VARCHAR2(100),
    UPDATED_AT              TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_BM_QUERY_EXPECTATION PRIMARY KEY (EXPECTATION_ID),
    CONSTRAINT CK_BM_QE_DB_TYPE CHECK (TARGET_DB_TYPE IN ('SYBASE', 'ORACLE')),
    CONSTRAINT CK_BM_QE_VALUE_TYPE CHECK (EXPECTED_VALUE_TYPE IN ('SCALAR', 'ROW_COUNT', 'JSON', 'HASH')),
    CONSTRAINT CK_BM_QE_OPERATOR CHECK (EXPECTED_OPERATOR IN ('EQ', 'NE', 'GT', 'GTE', 'LT', 'LTE', 'CONTAINS')),
    CONSTRAINT CK_BM_QE_ENABLED_YN CHECK (ENABLED_YN IN ('Y', 'N')),
    CONSTRAINT CK_BM_QE_SCHEDULE_YN CHECK (SCHEDULE_ENABLED_YN IN ('Y', 'N')),
    CONSTRAINT CK_BM_QE_NOTIFY_YN CHECK (NOTIFY_ENABLED_YN IN ('Y', 'N')),
    CONSTRAINT CK_BM_QE_NOTIFY_SUCCESS_YN CHECK (NOTIFY_ON_SUCCESS_YN IN ('Y', 'N'))
);
```

### 7.2 신규 테이블: 단일쿼리 검증 결과

```sql
CREATE TABLE BM_QUERY_EXPECTATION_RESULT (
    RESULT_ID               NUMBER(19)      NOT NULL,
    EXPECTATION_ID          NUMBER(19)      NOT NULL,
    REQUESTED_BY            VARCHAR2(100),
    REQUESTED_AT            TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    STARTED_AT              TIMESTAMP,
    FINISHED_AT             TIMESTAMP,
    RESULT_STATUS           VARCHAR2(20)    DEFAULT 'REQUESTED' NOT NULL,
    ACTUAL_ROW_COUNT        NUMBER(19),
    ACTUAL_VALUE            CLOB,
    ACTUAL_RESULT_HASH      VARCHAR2(128),
    EXPECTED_VALUE          CLOB,
    MISMATCH_SUMMARY        VARCHAR2(4000),
    ERROR_MESSAGE           CLOB,
    CONSTRAINT PK_BM_QUERY_EXPECTATION_RESULT PRIMARY KEY (RESULT_ID),
    CONSTRAINT FK_BM_QER_EXPECTATION FOREIGN KEY (EXPECTATION_ID)
        REFERENCES BM_QUERY_EXPECTATION (EXPECTATION_ID),
    CONSTRAINT CK_BM_QER_STATUS CHECK (RESULT_STATUS IN ('REQUESTED', 'RUNNING', 'SUCCESS', 'FAIL', 'ERROR'))
);
```

### 7.3 신규 테이블: 알림 이력

```sql
CREATE TABLE BM_VALIDATION_NOTIFICATION (
    NOTIFICATION_ID         NUMBER(19)      NOT NULL,
    VALIDATION_TYPE         VARCHAR2(20)    NOT NULL,
    RULE_ID                 NUMBER(19)      NOT NULL,
    RESULT_ID               NUMBER(19)      NOT NULL,
    RESULT_STATUS           VARCHAR2(20)    NOT NULL,
    TITLE                   VARCHAR2(300)   NOT NULL,
    MESSAGE                 CLOB,
    RECIPIENTS              VARCHAR2(1000),
    READ_YN                 CHAR(1)         DEFAULT 'N' NOT NULL,
    CREATED_AT              TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_BM_VALIDATION_NOTIFICATION PRIMARY KEY (NOTIFICATION_ID),
    CONSTRAINT CK_BM_VN_TYPE CHECK (VALIDATION_TYPE IN ('PAIR', 'SINGLE')),
    CONSTRAINT CK_BM_VN_READ_YN CHECK (READ_YN IN ('Y', 'N'))
);
```

### 7.4 기존 테이블 확장 권장

`BM_QUERY_COMPARISON`에 알림 컬럼을 추가한다.

```sql
ALTER TABLE BM_QUERY_COMPARISON ADD (
    NOTIFY_ENABLED_YN       CHAR(1) DEFAULT 'Y' NOT NULL,
    NOTIFY_ON_SUCCESS_YN    CHAR(1) DEFAULT 'N' NOT NULL,
    NOTIFY_RECIPIENTS       VARCHAR2(1000)
);
```

## 8. 백엔드 설계

### 8.1 패키지/클래스

기존 `QueryComparison*`는 쿼리쌍 비교 전용으로 유지한다.

신규 단일쿼리 검증:

- `QueryExpectationController`
- `QueryExpectationService`
- `QueryExpectationScheduler`
- `QueryExpectationMapper`
- `QueryExpectationDto`
- `QueryExpectationResultDto`
- `QueryExpectationSearchConditionDto`

공통화 대상:

- `ValidationQueryExecutor`
- `ValidationResultCanonicalizer`
- `ValidationNotificationService`
- `CronScheduleSupport`

단, 초기 구현에서는 과도한 추상화를 피하고 중복이 명확해지는 시점에 공통화한다.

### 8.2 URL

쿼리쌍 비교:

- `GET /query-comparisons`
- `POST /query-comparisons`
- `GET /query-comparisons/{comparisonId}/edit`
- `POST /query-comparisons/{comparisonId}/delete`
- `POST /query-comparisons/{comparisonId}/request`

단일쿼리 기대값 검증:

- `GET /query-expectations`
- `POST /query-expectations`
- `GET /query-expectations/{expectationId}/edit`
- `POST /query-expectations/{expectationId}/delete`
- `POST /query-expectations/{expectationId}/request`

통합 결과:

- `GET /validation-results`
- `GET /validation-results/pair/{resultId}`
- `GET /validation-results/single/{resultId}`

알림:

- `GET /validation-notifications`
- `POST /validation-notifications/{notificationId}/read`

## 9. 쿼리 실행 안전장치

필수 제한:

- `SELECT` 또는 `WITH`로 시작하는 쿼리만 허용
- 끝의 세미콜론 제거 후 실행
- 다중 statement 금지
- 최대 조회 row 수 제한
- 쿼리 타임아웃 설정
- fetch size 설정
- 등록 시 실제 DB에 `PreparedStatement`로 검증
- 실행 계정은 조회 전용 DB 계정 사용

권장 제한:

- 금지 키워드 검사: `insert`, `update`, `delete`, `merge`, `drop`, `alter`, `truncate`, `grant`, `revoke`, `execute`, `call`
- 쿼리별 최대 실행시간 설정
- 결과 CLOB 저장 시 최대 길이 제한
- 대량 결과는 전체 저장하지 않고 요약/hash 중심으로 저장

## 10. 비교/검증 규칙

### 10.1 쿼리쌍 비교 정규화

- 컬럼명은 대문자로 변환
- 컬럼 순서 차이를 줄이기 위해 컬럼명 기준 정렬
- null은 명시 문자열로 정규화
- 날짜/숫자 포맷 차이 처리 정책을 문서화
- row 순서가 의미 있는지 옵션화 검토

기본 정책:

- 사용자가 정렬 기준을 쿼리에 직접 포함한다.
- 서비스는 반환 row 순서를 그대로 비교한다.

### 10.2 단일쿼리 기대값 검증

- `ROW_COUNT`: 실제 row 수와 기대값 비교
- `SCALAR`: 첫 번째 row의 첫 번째 컬럼 값을 기대값과 비교
- `JSON`: 전체 결과를 정규화 JSON으로 변환 후 비교
- `HASH`: 전체 결과 정규화 hash와 기대값 hash 비교

숫자 비교 연산자는 실제값과 기대값이 숫자로 파싱 가능해야 한다.

## 11. 화면 요구사항

### 11.1 쿼리쌍 비교 업무 목록/등록

- 검색 영역
- 등록/수정 폼
- Sybase 쿼리 textarea
- Oracle 쿼리 textarea
- cron 입력
- 사용/스케줄/알림 체크박스
- 실행 버튼
- 최근 결과 배지

### 11.2 단일쿼리 기대값 업무 목록/등록

- 대상 DB 선택
- 쿼리 textarea
- 기대값 타입 선택
- 연산자 선택
- 기대값 textarea
- cron 입력
- 사용/스케줄/알림 체크박스
- 실행 버튼
- 최근 결과 배지

### 11.3 결과 목록

- 업무 유형 배지
- 업무명
- 상태 배지
- 요청자
- 요청시각
- 소요시간
- 요약
- 상세 링크

### 11.4 결과 상세

- 실행 메타 정보
- 쿼리 전문
- 기대값/실제값
- hash
- 불일치 요약
- 오류 전문

## 12. 설정

`application.yml` 또는 profile별 yml에 다음 설정을 둔다.

```yaml
query:
  compare:
    scheduler:
      fixed-delay-ms: 60000
    max-compare-rows: 10000
    query-timeout-seconds: 30
    sybase:
      url:
      username:
      password:
      driver-class-name:
    oracle:
      url:
      username:
      password:
      driver-class-name:
  validation:
    notification:
      base-url: http://localhost:8080
      default-recipients:
```

Sybase JDBC 드라이버 의존성은 운영 환경에서 사용하는 드라이버 기준으로 추가한다.

## 13. 테스트 기준

단위 테스트:

- SELECT/WITH 쿼리 검증
- 금지 쿼리 차단
- cron due 판단
- 쿼리쌍 결과 정규화/hash
- 단일쿼리 기대값 비교 연산자
- 알림 발생 조건

통합 테스트:

- 업무 등록/수정/중지
- 수동 실행
- 스케줄 대상 조회
- 결과 목록/상세 조회
- 오류 발생 시 `ERROR` 결과 저장

화면 확인:

- 좌측 메뉴 노출
- 등록 폼 validation
- 결과 상태 배지
- 모바일/좁은 화면에서 textarea와 테이블 깨짐 없음

## 14. 개발 순서

1. 기존 쿼리쌍 비교 메뉴명을 업무 용어에 맞게 정리한다.
2. 기존 쿼리쌍 비교 테이블에 알림 컬럼을 추가한다.
3. 단일쿼리 기대값 테이블과 결과 테이블을 추가한다.
4. `QueryExpectation*` controller/service/mapper/dto/template을 구현한다.
5. 단일쿼리 수동 실행을 구현한다.
6. 단일쿼리 cron scheduler를 구현한다.
7. 통합 결과 목록 또는 별도 결과 목록을 구현한다.
8. 알림 이력 저장 기능을 추가한다.
9. 좌측 메뉴를 최종 정리한다.
10. 테스트와 운영 설정 문서를 보강한다.

## 15. 완료 기준

- 기존 Spring Batch 모니터링 메뉴와 조회 기능이 기존처럼 동작한다.
- 사용자가 Sybase/Oracle 쿼리쌍 비교 업무를 등록하고 수동/자동 실행할 수 있다.
- 사용자가 단일쿼리 기대값 검증 업무를 등록하고 수동/자동 실행할 수 있다.
- 불일치와 오류가 결과 이력에 저장된다.
- 불일치와 오류가 알림 이력에 저장된다.
- 좌측 메뉴에서 각 기능으로 이동할 수 있다.
- 운영자가 매일 특정 시간 검증 결과를 확인할 수 있다.

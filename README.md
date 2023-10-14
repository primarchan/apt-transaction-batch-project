# 호갱노노 Batch 시스템 프로젝트
- 국토교통부 아파트 매매 실거래가 공공 데이터 기반 Spring Batch 프로젝트

## TECH-STACK
- Java 11
- Spring Boot 2.7.16
- Spring Batch 2.7.16
- Spring Data JPA 2.7.16
- Spring Data JDBC 2.7.16
- MySQL
- H2 DB
- Gradle 8.2.1
- Lombok 1.18.30
- JUnit5
- IntelliJ IDEA 2022.1.4 (Ultimate Edition)

## REQUIREMENT
### 동 코드 Migration Batch
- 정의: (데이터 생성 용으로) 법정동 파일을 DB 테이블에 저장한다. 
- Batch 주기 : 최초, 데이터가 수정 되었을 시
- 데이터 저장 : 법정동 파일을 DB 테이블 (`lawd`) 에 저장한다.

### 실거래가 수집 Batch 설계 요구사항
- 정의 : 매일 실거래가 데이터를 가져와 DB 에 저장한다.
- Batch 주기 : 매일 새벽 1시 (트래픽이 적은 시간)
- Reader : 법정동 '구' 코드 (`lawdCd`) 불러오기
- Processor : '구' 마다 현재 월에 대한 API 호출
- Writer : 새롭게 생성된 실거래가 정보만 DB 에 Upsert (Update + Insert)

### 실거래가 알림 Batch 설계 요구사항
- 정의 : 유저가 관심 설정한 구에 대해 실거래가 정보를 알린다.
- Batch 주기 : 매일 오전 8시 (유저가 알림을 받아야 할 시기)
- Reader : 유저 관심 테이블을 조회하며 알림대상을 추출
- Processor : 대상 아파트 데이터 -> 전송용 데이터로 변환
- Writer : 전송 인터페이스 구현

## BUILD & RUN
- 특정 Job 실행 시 Program arguments 에 Job 이름 추가
    - `--spring.bathc.job.names=${Job 이름}`
- JobParameter 추가
    - `--spring.bathc.job.names=${Job 이름} -${JobParamter 이름}=${JobParameter Value}`
- Window/Mac OS 에서 Build 한 파일이 있는 곳까지 명령프롬프트/터미널을 통해 접근해서 실행
    - `.gradlew bootJar`
    - `java -jar build/libs/${빌드한 파일}.jar --spring.batch.job.names=${Job 이름}`

## INFRASTRUCTURE
### Docker
- docker-compose.yml
- `docker-compse up -d`

## DATABASE
<details>
<summary>DDL 상세보기 (클릭) </summary>

```sql
-- 동 코드 테이블 생성
create table lawd
(
  lawd_id        bigint auto_increment primary key comment '법정동 코드 ID',
  lawd_cd        char(10) not null comment '법정동 코드',
  lawd_dong      varchar(100) not null comment '법정동 명',
  exist         tinyint(1) not null comment '존폐 여부',
  created_at    datetime not null comment '생성 일시',
  updated_at    datetime not null comment '수정 일시',
  constraint uk_lawdcd unique (lawd_cd)
);

-- 아파트 테이블 생성
create table apt
(
    apt_id      bigint auto_increment primary key comment '아파트 ID',
    apt_name    varchar(40) not null comment '아파트 명',
    jibun       varchar(20) not null comment '지번 주소',
    dong        varchar(40) not null comment '법정동',
    gu_lawd_cd  char(5) not null comment '법정구 코드',
    built_year  int not null comment '건축년도',
    created_at  datetime not null comment '생성 일시',
    updated_at  datetime not null comment '수정 일시'
);

-- 아퍄트 거래 테이블 생성
create table apt_deal
(
    apt_deal_id         bigint auto_increment primary key comment '아파트 거래 ID',
    apt_id              bigint not null comment '아파트 ID',
    exclusive_area      double not null comment '전용면적',
    deal_date           date not null comment '계약 일자',
    deal_amount         bigint not null comment '거래 금액',
    floor               int not null comment '층',
    deal_canceled       tinyint(1) default 0 not null comment '해제 여부',
    deal_canceled_date  date null comment '해제 사유 발생일',
    created_at  datetime not null comment '생성 일시',
    updated_at  datetime not null comment '수정 일시'
);

-- 아파트 거래 알림 테이블
create table apt_notification
(
    apt_notification_id bigint auto_increment primary key comment '아파트 거래 알림 ID',
    email               varchar(100) not null comment '이메일',
    gu_lawd_cd          char(5) not null comment '법정구 코드',
    enabled             tinyint(1) not null comment '관심 여부',
    created_at          datetime not null comment '생성 일시',
    updated_at          datetime not null comment '수정 일시',
    constraint uk_email_gulawdcd unique (email, gu_lawd_cd)
);
```
</details>
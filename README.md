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
  lawd_id        bigint auto_increment primary key,
  lawd_cd        char(10) not null,
  lawd_dong      varchar(100) not null,
  exist         tinyint(1) not null,
  created_at    datetime not null,
  updated_at    datetime not null,
  constraint uk_lawdcd unique (lawd_cd)
);

-- 아파트 테이블 생성
create table apt
(
    apt_id      bigint auto_increment primary key,
    apt_name    varchar(40) not null,
    jibun       varchar(20) not null,
    dong        varchar(40) not null,
    gu_lawd_cd  char(5) not null,
    built_year  int not null,
    created_at  datetime not null,
    updated_at  datetime not null
);

-- 아퍄트 거래 테이블 생성
create table apt_deal
(
    apt_deal_id         bigint auto_increment primary key,
    apt_id              bigint not null,
    exclusive_area      double not null,
    deal_date           date not null,
    deal_amount         bigint not null,
    floor               int not null,
    deal_canceled       tinyint(1) default 0 not null,
    deal_canceled_date  date null,
    created_at  datetime not null,
    updated_at  datetime not null
);

-- 아파트 거래 알림 테이블
create table apt_notification
(
    apt_notification_id bigint auto_increment primary key,
    email               varchar(100) not null,
    gu_lawd_cd          char(5) not null,
    enabled             tinyint(1) not null,
    created_at          datetime not null,
    updated_at          datetime not null,
    constraint uk_email_gulawdcd unique (email, gu_lawd_cd)
);
```
</details>
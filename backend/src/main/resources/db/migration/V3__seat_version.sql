-- 1단계 낙관적 락에서 쓰는 버전 컬럼
alter table seat add column version bigint not null default 0;

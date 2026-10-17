-- 1단계 UNIQUE_KEY 전략: 좌석 번호를 기본 키로 둔 선점 테이블.
-- 먼저 insert에 성공한 한 명만 예약을 이어 간다.
create table seat_claim
(
    seat_id    bigint primary key references seat (id),
    user_id    varchar(50) not null,
    claimed_at timestamptz not null default now()
);

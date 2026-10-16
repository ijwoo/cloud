create table concert
(
    id        bigserial primary key,
    title     varchar(100) not null,
    artist    varchar(100) not null,
    venue     varchar(100) not null,
    starts_at timestamptz  not null
);

create table seat
(
    id         bigserial primary key,
    concert_id bigint      not null references concert (id),
    section    varchar(10) not null,
    row_label  varchar(5)  not null,
    number     int         not null,
    price      int         not null,
    status     varchar(20) not null default 'AVAILABLE'
);

create index idx_seat_concert on seat (concert_id);

-- 0단계에서는 seat_id에 unique 제약을 일부러 걸지 않는다.
-- 애플리케이션 코드만으로 초과 예매를 막을 수 있는지부터 확인하기 위해서다.
create table reservation
(
    id         bigserial primary key,
    seat_id    bigint       not null references seat (id),
    user_id    varchar(50)  not null,
    created_at timestamptz  not null
);

create index idx_reservation_seat on reservation (seat_id);

-- 2단계 부하 테스트용 대형 공연. 100열 x 50석 = 5,000석.
insert into concert (title, artist, venue, starts_at)
values ('RUSH ARENA', 'The Rushers', '고척스카이돔 (부하 테스트용)', '2027-01-30 18:00:00+09');

insert into seat (concert_id, section, row_label, number, price)
select c.id,
       case when r.idx <= 10 then 'VIP' else 'R' end,
       lpad(r.idx::text, 3, '0'),
       n.num,
       case when r.idx <= 10 then 165000 else 132000 end
from concert c
         cross join generate_series(1, 100) as r(idx)
         cross join generate_series(1, 50) as n(num)
where c.title = 'RUSH ARENA';

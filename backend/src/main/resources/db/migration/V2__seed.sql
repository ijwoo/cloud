insert into concert (title, artist, venue, starts_at)
values ('RUSH LIVE 2026', 'The Rushers', '올림픽공원 KSPO DOME', '2026-12-24 19:00:00+09');

-- A~J열 x 1~20번 = 200석. 앞 3열은 VIP.
insert into seat (concert_id, section, row_label, number, price)
select c.id,
       case when r.idx <= 3 then 'VIP' else 'R' end,
       chr(64 + r.idx),
       n.num,
       case when r.idx <= 3 then 165000 else 132000 end
from concert c
         cross join generate_series(1, 10) as r(idx)
         cross join generate_series(1, 20) as n(num)
where c.title = 'RUSH LIVE 2026';

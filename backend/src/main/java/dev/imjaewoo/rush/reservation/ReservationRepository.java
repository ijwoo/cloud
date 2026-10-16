package dev.imjaewoo.rush.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    long countBySeatId(Long seatId);

    long countBySeatConcertId(Long concertId);

    /** 예약이 2건 이상 잡힌 좌석 수. 0이 아니면 초과 예매가 일어난 것. */
    @Query("""
            select count(*) from (
                select r.seat.id as seatId from Reservation r
                where r.seat.concert.id = :concertId
                group by r.seat.id
                having count(r) > 1
            ) doubled
            """)
    long countDoubleBookedSeats(Long concertId);

    @Modifying
    @Query("delete from Reservation r where r.seat.id = :seatId")
    int deleteBySeatId(Long seatId);

    @Modifying
    @Query("delete from Reservation r where r.seat.id in (select s.id from Seat s where s.concert.id = :concertId)")
    int deleteByConcertId(Long concertId);
}

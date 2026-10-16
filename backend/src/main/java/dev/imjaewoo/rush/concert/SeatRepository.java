package dev.imjaewoo.rush.concert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByConcertIdOrderByIdAsc(Long concertId);

    long countByConcertIdAndStatus(Long concertId, SeatStatus status);

    @Modifying
    @Query("update Seat s set s.status = dev.imjaewoo.rush.concert.SeatStatus.AVAILABLE where s.concert.id = :concertId")
    int resetByConcertId(Long concertId);
}

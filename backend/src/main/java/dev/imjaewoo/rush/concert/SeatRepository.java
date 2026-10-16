package dev.imjaewoo.rush.concert;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByConcertIdOrderByIdAsc(Long concertId);

    long countByConcertIdAndStatus(Long concertId, SeatStatus status);

    /** select ... for update */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :seatId")
    Optional<Seat> findByIdForUpdate(Long seatId);

    /** 읽었을 때의 버전 그대로일 때만 바꾼다. 0이 반환되면 그 사이 누군가 먼저 바꾼 것. */
    @Modifying
    @Query("""
            update Seat s set s.status = dev.imjaewoo.rush.concert.SeatStatus.RESERVED, s.version = s.version + 1
            where s.id = :seatId and s.version = :version
            """)
    int reserveIfVersionMatches(Long seatId, long version);

    /** 확인과 변경을 UPDATE 한 문장으로. 0이 반환되면 이미 예매된 좌석. */
    @Modifying
    @Query("""
            update Seat s set s.status = dev.imjaewoo.rush.concert.SeatStatus.RESERVED, s.version = s.version + 1
            where s.id = :seatId and s.status = dev.imjaewoo.rush.concert.SeatStatus.AVAILABLE
            """)
    int reserveIfAvailable(Long seatId);

    @Modifying
    @Query("update Seat s set s.status = dev.imjaewoo.rush.concert.SeatStatus.AVAILABLE where s.concert.id = :concertId")
    int resetByConcertId(Long concertId);

    @Modifying
    @Query("update Seat s set s.status = dev.imjaewoo.rush.concert.SeatStatus.AVAILABLE where s.id = :seatId")
    int resetById(Long seatId);
}

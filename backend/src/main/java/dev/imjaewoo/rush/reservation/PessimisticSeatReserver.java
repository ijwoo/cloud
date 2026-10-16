package dev.imjaewoo.rush.reservation;

import dev.imjaewoo.rush.common.ConflictException;
import dev.imjaewoo.rush.common.NotFoundException;
import dev.imjaewoo.rush.concert.Seat;
import dev.imjaewoo.rush.concert.SeatRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * NONE과 로직은 같고, 좌석을 읽을 때 행 잠금을 건다.
 * 같은 좌석을 노리는 트랜잭션은 앞 트랜잭션이 끝날 때까지 기다린 뒤 RESERVED를 읽게 된다.
 */
@Component
public class PessimisticSeatReserver implements SeatReserver {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    public PessimisticSeatReserver(SeatRepository seatRepository, ReservationRepository reservationRepository) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public LockStrategy strategy() {
        return LockStrategy.PESSIMISTIC;
    }

    @Override
    @Transactional
    public Reservation reserve(Long seatId, String userId) {
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new NotFoundException("좌석을 찾을 수 없습니다. id=" + seatId));
        if (!seat.isAvailable()) {
            throw new ConflictException("이미 예매된 좌석입니다. id=" + seatId);
        }
        seat.reserve();
        return reservationRepository.save(new Reservation(seat, userId));
    }
}

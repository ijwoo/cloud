package dev.imjaewoo.rush.reservation;

import dev.imjaewoo.rush.common.ConflictException;
import dev.imjaewoo.rush.common.NotFoundException;
import dev.imjaewoo.rush.concert.Seat;
import dev.imjaewoo.rush.concert.SeatRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * "조회 → 확인 → 저장"을 그대로 한 버전.
 * 혼자 누를 때는 문제가 없지만, 같은 좌석에 요청이 동시에 들어오면
 * 여러 트랜잭션이 모두 AVAILABLE을 읽고 각자 예약을 저장한다.
 */
@Component
public class NoLockSeatReserver implements SeatReserver {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    public NoLockSeatReserver(SeatRepository seatRepository, ReservationRepository reservationRepository) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public LockStrategy strategy() {
        return LockStrategy.NONE;
    }

    @Override
    @Transactional
    public Reservation reserve(Long seatId, String userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new NotFoundException("좌석을 찾을 수 없습니다. id=" + seatId));
        if (!seat.isAvailable()) {
            throw new ConflictException("이미 예매된 좌석입니다. id=" + seatId);
        }
        seat.reserve();
        return reservationRepository.save(new Reservation(seat, userId));
    }
}

package dev.imjaewoo.rush.reservation;

import dev.imjaewoo.rush.common.ConflictException;
import dev.imjaewoo.rush.common.NotFoundException;
import dev.imjaewoo.rush.concert.Seat;
import dev.imjaewoo.rush.concert.SeatRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 잠그지 않고 읽은 뒤, 저장할 때 "내가 읽은 버전 그대로인가"를 확인한다.
 * 좌석은 한 번 팔리면 끝이라 버전 충돌 시 재시도하지 않고 바로 거절한다.
 */
@Component
public class OptimisticSeatReserver implements SeatReserver {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    public OptimisticSeatReserver(SeatRepository seatRepository, ReservationRepository reservationRepository) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public LockStrategy strategy() {
        return LockStrategy.OPTIMISTIC;
    }

    @Override
    @Transactional
    public Reservation reserve(Long seatId, String userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new NotFoundException("좌석을 찾을 수 없습니다. id=" + seatId));
        if (!seat.isAvailable() || seatRepository.reserveIfVersionMatches(seatId, seat.getVersion()) == 0) {
            throw new ConflictException("이미 예매된 좌석입니다. id=" + seatId);
        }
        return reservationRepository.save(new Reservation(seat, userId));
    }
}

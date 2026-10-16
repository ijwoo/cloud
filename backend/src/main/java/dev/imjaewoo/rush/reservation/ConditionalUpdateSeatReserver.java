package dev.imjaewoo.rush.reservation;

import dev.imjaewoo.rush.common.ConflictException;
import dev.imjaewoo.rush.common.NotFoundException;
import dev.imjaewoo.rush.concert.SeatRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * "AVAILABLE이면 RESERVED로"를 UPDATE 한 문장에 담는다.
 * 확인과 변경 사이에 틈이 없으므로 먼저 읽을 필요가 없다.
 */
@Component
public class ConditionalUpdateSeatReserver implements SeatReserver {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    public ConditionalUpdateSeatReserver(SeatRepository seatRepository, ReservationRepository reservationRepository) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public LockStrategy strategy() {
        return LockStrategy.CONDITIONAL_UPDATE;
    }

    @Override
    @Transactional
    public Reservation reserve(Long seatId, String userId) {
        if (seatRepository.reserveIfAvailable(seatId) == 0) {
            if (!seatRepository.existsById(seatId)) {
                throw new NotFoundException("좌석을 찾을 수 없습니다. id=" + seatId);
            }
            throw new ConflictException("이미 예매된 좌석입니다. id=" + seatId);
        }
        return reservationRepository.save(new Reservation(seatRepository.getReferenceById(seatId), userId));
    }
}

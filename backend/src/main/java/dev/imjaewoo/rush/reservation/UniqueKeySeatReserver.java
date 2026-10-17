package dev.imjaewoo.rush.reservation;

import dev.imjaewoo.rush.common.ConflictException;
import dev.imjaewoo.rush.common.NotFoundException;
import dev.imjaewoo.rush.concert.Seat;
import dev.imjaewoo.rush.concert.SeatRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 판단을 DB 제약에 맡긴다. 좌석 번호가 기본 키인 seat_claim에 insert를 시도하고,
 * 성공한 한 명만 좌석 상태 변경과 예약 저장으로 넘어간다.
 * reservation 테이블에 직접 unique를 걸면 NONE 전략의 재현까지 막히므로 테이블을 분리했다.
 */
@Component
public class UniqueKeySeatReserver implements SeatReserver {

    private final SeatRepository seatRepository;
    private final SeatClaimRepository seatClaimRepository;
    private final ReservationRepository reservationRepository;

    public UniqueKeySeatReserver(SeatRepository seatRepository,
                                 SeatClaimRepository seatClaimRepository,
                                 ReservationRepository reservationRepository) {
        this.seatRepository = seatRepository;
        this.seatClaimRepository = seatClaimRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public LockStrategy strategy() {
        return LockStrategy.UNIQUE_KEY;
    }

    @Override
    @Transactional
    public Reservation reserve(Long seatId, String userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new NotFoundException("좌석을 찾을 수 없습니다. id=" + seatId));
        if (!seat.isAvailable() || !seatClaimRepository.claim(seatId, userId)) {
            throw new ConflictException("이미 예매된 좌석입니다. id=" + seatId);
        }
        seat.reserve();
        return reservationRepository.save(new Reservation(seat, userId));
    }
}

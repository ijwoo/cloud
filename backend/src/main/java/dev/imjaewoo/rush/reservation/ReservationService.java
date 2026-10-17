package dev.imjaewoo.rush.reservation;

import dev.imjaewoo.rush.concert.SeatRepository;
import dev.imjaewoo.rush.concert.SeatStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservationService {

    private final Map<LockStrategy, SeatReserver> reservers = new EnumMap<>(LockStrategy.class);
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final SeatClaimRepository seatClaimRepository;

    public ReservationService(List<SeatReserver> reservers,
                              SeatRepository seatRepository,
                              ReservationRepository reservationRepository,
                              SeatClaimRepository seatClaimRepository) {
        reservers.forEach(r -> this.reservers.put(r.strategy(), r));
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.seatClaimRepository = seatClaimRepository;
    }

    public Reservation reserve(Long seatId, String userId, LockStrategy strategy) {
        return reservers.get(strategy).reserve(seatId, userId);
    }

    @Transactional(readOnly = true)
    public ReservationStats stats(Long concertId) {
        return new ReservationStats(
                seatRepository.countByConcertIdAndStatus(concertId, SeatStatus.RESERVED),
                reservationRepository.countBySeatConcertId(concertId),
                reservationRepository.countDoubleBookedSeats(concertId)
        );
    }

    /** 실험을 반복할 수 있도록 공연의 예약을 모두 지우고 좌석을 되돌린다. */
    @Transactional
    public void reset(Long concertId) {
        reservationRepository.deleteByConcertId(concertId);
        seatClaimRepository.deleteByConcertId(concertId);
        seatRepository.resetByConcertId(concertId);
    }

    @Transactional
    public void resetSeat(Long seatId) {
        reservationRepository.deleteBySeatId(seatId);
        seatClaimRepository.deleteBySeatId(seatId);
        seatRepository.resetById(seatId);
    }
}

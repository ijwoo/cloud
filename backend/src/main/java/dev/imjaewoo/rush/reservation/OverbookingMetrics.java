package dev.imjaewoo.rush.reservation;

import dev.imjaewoo.rush.concert.ConcertRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 공연별 "예약이 2건 이상 잡힌 좌석 수"를 게이지로 노출한다.
 * 부하 테스트 중 Grafana에서 중복 판매가 쌓이는 모습을 보기 위한 것.
 */
@Component
public class OverbookingMetrics {

    private final ConcertRepository concertRepository;
    private final ReservationRepository reservationRepository;
    private final MeterRegistry meterRegistry;

    public OverbookingMetrics(ConcertRepository concertRepository,
                              ReservationRepository reservationRepository,
                              MeterRegistry meterRegistry) {
        this.concertRepository = concertRepository;
        this.reservationRepository = reservationRepository;
        this.meterRegistry = meterRegistry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        concertRepository.findAll().forEach(concert -> {
            Long concertId = concert.getId();
            Gauge.builder("rush.overbooked.seats", () -> reservationRepository.countDoubleBookedSeats(concertId))
                    .tag("concert", concert.getTitle())
                    .register(meterRegistry);
            Gauge.builder("rush.reservations", () -> reservationRepository.countBySeatConcertId(concertId))
                    .tag("concert", concert.getTitle())
                    .register(meterRegistry);
        });
    }
}

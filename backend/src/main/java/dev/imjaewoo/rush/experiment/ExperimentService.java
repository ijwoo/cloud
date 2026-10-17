package dev.imjaewoo.rush.experiment;

import dev.imjaewoo.rush.common.ConflictException;
import dev.imjaewoo.rush.common.NotFoundException;
import dev.imjaewoo.rush.concert.Seat;
import dev.imjaewoo.rush.concert.SeatRepository;
import dev.imjaewoo.rush.reservation.ReservationRepository;
import dev.imjaewoo.rush.reservation.ReservationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 여러 명이 동시에 예매를 누르는 상황을 서버 안에서 재현한다.
 * 화면의 "실험실"과 동시성 테스트가 같은 코드를 쓴다.
 */
@Service
public class ExperimentService {

    private static final int THREADS = 32;

    private final ReservationService reservationService;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    public ExperimentService(ReservationService reservationService,
                             SeatRepository seatRepository,
                             ReservationRepository reservationRepository) {
        this.reservationService = reservationService;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    public ExperimentResult run(ExperimentRequest request) throws InterruptedException {
        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new NotFoundException("좌석을 찾을 수 없습니다. id=" + request.seatId()));
        Long concertId = seatRepository.findConcertIdBySeatId(seat.getId());

        List<Long> targets = switch (request.scenario()) {
            case HOT -> {
                reservationService.resetSeat(seat.getId());
                yield List.of(seat.getId());
            }
            case SPREAD -> {
                reservationService.reset(concertId);
                yield seatRepository.findByConcertIdOrderByIdAsc(concertId).stream().map(Seat::getId).toList();
            }
        };

        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(request.users());

        long elapsed;
        try (ExecutorService executor = Executors.newFixedThreadPool(THREADS)) {
            for (int i = 0; i < request.users(); i++) {
                String userId = "lab-" + i;
                Long target = targets.get(ThreadLocalRandom.current().nextInt(targets.size()));
                executor.submit(() -> {
                    try {
                        start.await();
                        reservationService.reserve(target, userId, request.strategy());
                        succeeded.incrementAndGet();
                    } catch (ConflictException e) {
                        rejected.incrementAndGet();
                    } catch (Exception e) {
                        failed.incrementAndGet();
                    } finally {
                        done.countDown();
                    }
                });
            }
            long startedAt = System.nanoTime();
            start.countDown();
            done.await();
            elapsed = (System.nanoTime() - startedAt) / 1_000_000;
        }

        long reservations;
        long reservedSeats;
        if (request.scenario() == Scenario.HOT) {
            reservations = reservationRepository.countBySeatId(seat.getId());
            reservedSeats = reservations > 0 ? 1 : 0;
        } else {
            reservations = reservationRepository.countBySeatConcertId(concertId);
            reservedSeats = reservationRepository.countReservedSeatsByConcertId(concertId);
        }

        return new ExperimentResult(
                request.strategy(),
                request.scenario(),
                request.users(),
                targets.size(),
                succeeded.get(),
                rejected.get(),
                failed.get(),
                reservations,
                reservedSeats,
                reservations - reservedSeats,
                elapsed
        );
    }
}

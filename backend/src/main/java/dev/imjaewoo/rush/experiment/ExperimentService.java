package dev.imjaewoo.rush.experiment;

import dev.imjaewoo.rush.common.ConflictException;
import dev.imjaewoo.rush.concert.SeatRepository;
import dev.imjaewoo.rush.reservation.ReservationRepository;
import dev.imjaewoo.rush.reservation.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 좌석 하나에 N명이 동시에 예매를 시도하는 상황을 서버 안에서 재현한다.
 * 화면의 "실험실"과 동시성 테스트가 같은 코드를 쓴다.
 */
@Service
public class ExperimentService {

    private static final int THREADS = 32;

    private final ReservationService reservationService;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final TransactionTemplate transactionTemplate;

    public ExperimentService(ReservationService reservationService,
                             SeatRepository seatRepository,
                             ReservationRepository reservationRepository,
                             TransactionTemplate transactionTemplate) {
        this.reservationService = reservationService;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public ExperimentResult run(ExperimentRequest request) throws InterruptedException {
        Long seatId = request.seatId();
        resetSeat(seatId);

        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(request.users());

        long elapsed;
        try (ExecutorService executor = Executors.newFixedThreadPool(THREADS)) {
            for (int i = 0; i < request.users(); i++) {
                String userId = "lab-" + i;
                executor.submit(() -> {
                    try {
                        start.await();
                        reservationService.reserve(seatId, userId, request.strategy());
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

        return new ExperimentResult(
                request.strategy(),
                request.users(),
                succeeded.get(),
                rejected.get(),
                failed.get(),
                reservationRepository.countBySeatId(seatId),
                elapsed
        );
    }

    private void resetSeat(Long seatId) {
        transactionTemplate.executeWithoutResult(status -> {
            reservationRepository.deleteBySeatId(seatId);
            seatRepository.resetById(seatId);
        });
    }
}

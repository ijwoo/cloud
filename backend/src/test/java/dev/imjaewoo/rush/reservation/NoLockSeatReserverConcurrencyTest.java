package dev.imjaewoo.rush.reservation;

import dev.imjaewoo.rush.TestcontainersConfiguration;
import dev.imjaewoo.rush.concert.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 0단계: 락 없이 "조회 → 확인 → 저장"만 하면 어떤 일이 생기는지 재현한다.
 * 이후 단계의 전략들은 같은 시나리오에서 예약이 정확히 1건이어야 한다.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class NoLockSeatReserverConcurrencyTest {

    private static final long CONCERT_ID = 1L;
    private static final long SEAT_ID = 1L;
    private static final int USERS = 100;

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    SeatRepository seatRepository;

    @BeforeEach
    void setUp() {
        reservationService.reset(CONCERT_ID);
    }

    @Test
    void 한_명이_예매하면_좌석이_RESERVED가_되고_두번째_요청은_거절된다() {
        reservationService.reserve(SEAT_ID, "user-1", LockStrategy.NONE);

        assertThat(seatRepository.findById(SEAT_ID).orElseThrow().isAvailable()).isFalse();
        assertThatThrownBy(() -> reservationService.reserve(SEAT_ID, "user-2", LockStrategy.NONE))
                .hasMessageContaining("이미 예매된 좌석");
    }

    @Test
    void 같은_좌석에_100명이_동시에_요청하면_초과_예매가_발생한다() throws InterruptedException {
        AtomicInteger succeeded = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(USERS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(USERS);

        try (ExecutorService executor = Executors.newFixedThreadPool(32)) {
            for (int i = 0; i < USERS; i++) {
                String userId = "user-" + i;
                executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        reservationService.reserve(SEAT_ID, userId, LockStrategy.NONE);
                        succeeded.incrementAndGet();
                    } catch (Exception ignored) {
                        // 이미 예매된 좌석 → 409
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            done.await();
        }

        long reservations = reservationRepository.countBySeatId(SEAT_ID);
        System.out.printf("[NONE] 요청 %d건 → 성공 %d건, 좌석 1개에 저장된 예약 %d건%n",
                USERS, succeeded.get(), reservations);

        assertThat(reservations).isEqualTo(succeeded.get());
        assertThat(reservations).isGreaterThan(1);
        assertThat(reservationService.stats(CONCERT_ID).doubleBookedSeats()).isEqualTo(1);
    }
}

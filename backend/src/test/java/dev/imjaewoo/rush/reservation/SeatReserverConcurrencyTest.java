package dev.imjaewoo.rush.reservation;

import dev.imjaewoo.rush.TestcontainersConfiguration;
import dev.imjaewoo.rush.experiment.ExperimentRequest;
import dev.imjaewoo.rush.experiment.ExperimentResult;
import dev.imjaewoo.rush.experiment.ExperimentService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 1단계: DB만으로 막는 전략들은 같은 시나리오에서 예약이 정확히 1건이어야 한다.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SeatReserverConcurrencyTest {

    private static final long SEAT_ID = 2L;
    private static final int USERS = 100;

    @Autowired
    ExperimentService experimentService;

    @ParameterizedTest
    @EnumSource(value = LockStrategy.class, names = {"PESSIMISTIC", "OPTIMISTIC", "CONDITIONAL_UPDATE"})
    void 같은_좌석에_100명이_동시에_요청해도_예약은_1건(LockStrategy strategy) throws InterruptedException {
        ExperimentResult result = experimentService.run(new ExperimentRequest(SEAT_ID, strategy, USERS));
        System.out.printf("[%s] %s%n", strategy, result);

        assertThat(result.reservations()).isEqualTo(1);
        assertThat(result.succeeded()).isEqualTo(1);
        assertThat(result.rejected()).isEqualTo(USERS - 1);
        assertThat(result.failed()).isZero();
    }
}

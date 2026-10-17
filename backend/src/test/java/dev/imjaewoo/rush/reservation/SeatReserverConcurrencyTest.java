package dev.imjaewoo.rush.reservation;

import dev.imjaewoo.rush.TestcontainersConfiguration;
import dev.imjaewoo.rush.experiment.ExperimentRequest;
import dev.imjaewoo.rush.experiment.ExperimentResult;
import dev.imjaewoo.rush.experiment.ExperimentService;
import dev.imjaewoo.rush.experiment.Scenario;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 1단계: DB만으로 막는 전략들은 어떤 시나리오에서도 같은 좌석을 두 번 팔면 안 된다.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SeatReserverConcurrencyTest {

    private static final long SEAT_ID = 2L;

    @Autowired
    ExperimentService experimentService;

    @ParameterizedTest
    @EnumSource(value = LockStrategy.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
    void 같은_좌석에_100명이_동시에_요청해도_예약은_1건(LockStrategy strategy) throws InterruptedException {
        ExperimentResult result = experimentService.run(new ExperimentRequest(SEAT_ID, strategy, Scenario.HOT, 100));
        System.out.printf("[%s] %s%n", strategy, result);

        assertThat(result.reservations()).isEqualTo(1);
        assertThat(result.succeeded()).isEqualTo(1);
        assertThat(result.rejected()).isEqualTo(99);
        assertThat(result.failed()).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = LockStrategy.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
    void 여러_좌석에_흩어져도_중복_판매는_0건(LockStrategy strategy) throws InterruptedException {
        ExperimentResult result = experimentService.run(new ExperimentRequest(SEAT_ID, strategy, Scenario.SPREAD, 500));
        System.out.printf("[%s] %s%n", strategy, result);

        assertThat(result.overbooked()).isZero();
        assertThat(result.failed()).isZero();
        assertThat(result.succeeded()).isEqualTo(result.reservedSeats());
        assertThat(result.succeeded() + result.rejected()).isEqualTo(500);
    }
}

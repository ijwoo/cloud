package dev.imjaewoo.rush.experiment;

import dev.imjaewoo.rush.reservation.LockStrategy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * @param seatId HOT이면 모두가 노리는 좌석, SPREAD이면 이 좌석이 속한 공연 전체가 대상
 */
public record ExperimentRequest(
        @NotNull Long seatId,
        @NotNull LockStrategy strategy,
        @NotNull Scenario scenario,
        @Min(1) @Max(1000) int users
) {
}

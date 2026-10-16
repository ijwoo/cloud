package dev.imjaewoo.rush.experiment;

import dev.imjaewoo.rush.reservation.LockStrategy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExperimentRequest(
        @NotNull Long seatId,
        @NotNull LockStrategy strategy,
        @Min(1) @Max(1000) int users
) {
}

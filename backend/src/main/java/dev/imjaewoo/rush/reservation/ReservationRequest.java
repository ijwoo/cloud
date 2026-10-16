package dev.imjaewoo.rush.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(@NotNull Long seatId, @NotBlank String userId) {
}

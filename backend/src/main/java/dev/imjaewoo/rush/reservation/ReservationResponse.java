package dev.imjaewoo.rush.reservation;

import java.time.OffsetDateTime;

public record ReservationResponse(Long id, Long seatId, String userId, OffsetDateTime createdAt) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getSeat().getId(),
                reservation.getUserId(),
                reservation.getCreatedAt()
        );
    }
}

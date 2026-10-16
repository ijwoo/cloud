package dev.imjaewoo.rush.reservation;

public interface SeatReserver {

    LockStrategy strategy();

    Reservation reserve(Long seatId, String userId);
}

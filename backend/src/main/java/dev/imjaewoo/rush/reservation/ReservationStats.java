package dev.imjaewoo.rush.reservation;

/**
 * @param reservedSeats      RESERVED 상태인 좌석 수
 * @param reservations       저장된 예약 건수
 * @param doubleBookedSeats  예약이 2건 이상 잡힌 좌석 수 (초과 예매)
 */
public record ReservationStats(long reservedSeats, long reservations, long doubleBookedSeats) {
}

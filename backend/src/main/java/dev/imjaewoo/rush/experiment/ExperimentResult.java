package dev.imjaewoo.rush.experiment;

import dev.imjaewoo.rush.reservation.LockStrategy;

/**
 * @param targetSeats   사용자들이 노린 좌석 수
 * @param succeeded     예매 성공 응답을 받은 사용자 수
 * @param rejected      "이미 예매된 좌석"으로 거절된 사용자 수
 * @param failed        그 밖의 오류(데드락, 타임아웃 등)로 실패한 사용자 수
 * @param reservations  실험이 끝난 뒤 DB에 남은 예약 건수
 * @param reservedSeats 예약이 1건 이상 있는 좌석 수
 * @param overbooked    같은 좌석이 두 번 이상 팔린 건수 (reservations - reservedSeats). 0이어야 정상
 */
public record ExperimentResult(
        LockStrategy strategy,
        Scenario scenario,
        int users,
        int targetSeats,
        int succeeded,
        int rejected,
        int failed,
        long reservations,
        long reservedSeats,
        long overbooked,
        long elapsedMillis
) {
}

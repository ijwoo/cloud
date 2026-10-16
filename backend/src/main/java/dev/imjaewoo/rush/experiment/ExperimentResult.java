package dev.imjaewoo.rush.experiment;

import dev.imjaewoo.rush.reservation.LockStrategy;

/**
 * @param succeeded    예매 성공 응답을 받은 사용자 수
 * @param rejected     "이미 예매된 좌석"으로 거절된 사용자 수
 * @param failed       그 밖의 오류(데드락, 타임아웃 등)로 실패한 사용자 수
 * @param reservations 실험이 끝난 뒤 DB에 남은 해당 좌석의 예약 건수. 1이어야 정상
 */
public record ExperimentResult(
        LockStrategy strategy,
        int users,
        int succeeded,
        int rejected,
        int failed,
        long reservations,
        long elapsedMillis
) {
}

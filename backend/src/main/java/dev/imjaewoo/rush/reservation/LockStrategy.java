package dev.imjaewoo.rush.reservation;

/**
 * 좌석 예매 시 동시성을 어떻게 다룰지. 단계가 진행될 때마다 하나씩 늘어난다.
 */
public enum LockStrategy {
    /** 아무 처리도 하지 않는다. 초과 예매를 재현하기 위한 출발점. */
    NONE,
    /** select ... for update 로 좌석 행을 잠그고 확인한다. */
    PESSIMISTIC,
    /** 읽은 버전과 같을 때만 UPDATE 한다. */
    OPTIMISTIC,
    /** 상태 확인과 변경을 UPDATE 한 문장으로 처리한다. */
    CONDITIONAL_UPDATE,
    /** 좌석 번호가 기본 키인 선점 테이블에 먼저 insert 한 사람만 통과한다. */
    UNIQUE_KEY
}

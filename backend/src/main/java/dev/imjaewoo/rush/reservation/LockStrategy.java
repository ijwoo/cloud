package dev.imjaewoo.rush.reservation;

/**
 * 좌석 예매 시 동시성을 어떻게 다룰지. 단계가 진행될 때마다 하나씩 늘어난다.
 */
public enum LockStrategy {
    /** 아무 처리도 하지 않는다. 초과 예매를 재현하기 위한 출발점. */
    NONE
}

package dev.imjaewoo.rush.queue;

/**
 * @param state      WAITING: 대기 중 / ADMITTED: 입장 완료(예매 가능) / EXPIRED: 대기열에 없음
 * @param ahead      내 앞에 있는 사람 수
 * @param waiting    지금 대기열 전체 인원
 * @param admitPerSecond 1초에 입장시키는 인원 (예상 대기 시간 계산용)
 */
public record QueueStatus(State state, long ahead, long waiting, int admitPerSecond) {

    public enum State {
        WAITING,
        ADMITTED,
        EXPIRED
    }
}

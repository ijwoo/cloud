package dev.imjaewoo.rush.reservation;

import dev.imjaewoo.rush.common.ConflictException;
import dev.imjaewoo.rush.common.RedisLock;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 좌석마다 Redis 락을 잡고, 그 안에서 NONE과 같은 "조회 → 확인 → 저장"을 한다.
 *
 * <p>락 해제는 반드시 트랜잭션 커밋 <b>이후</b>여야 한다. 이 메서드에 @Transactional을 붙이면
 * finally의 unlock이 커밋보다 먼저 실행되고, 다음 사람이 아직 커밋 안 된 AVAILABLE을 읽게 된다.
 * 그래서 트랜잭션은 안쪽(NoLockSeatReserver)에만 두었다.
 *
 * <p>락을 못 잡으면 기다리지 않고 바로 거절한다. 같은 좌석을 지금 누가 예매 중이라는 뜻이고,
 * 좌석은 한 명만 가질 수 있으므로 기다려 봐야 대부분 "이미 예매됨"이기 때문이다.
 */
@Component
public class RedisLockSeatReserver implements SeatReserver {

    private static final Duration LEASE_TIME = Duration.ofSeconds(3);

    private final RedisLock redisLock;
    private final NoLockSeatReserver noLockSeatReserver;

    public RedisLockSeatReserver(RedisLock redisLock, NoLockSeatReserver noLockSeatReserver) {
        this.redisLock = redisLock;
        this.noLockSeatReserver = noLockSeatReserver;
    }

    @Override
    public LockStrategy strategy() {
        return LockStrategy.REDIS_LOCK;
    }

    @Override
    public Reservation reserve(Long seatId, String userId) {
        String key = "lock:seat:" + seatId;
        String token = redisLock.tryLock(key, LEASE_TIME)
                .orElseThrow(() -> new ConflictException("다른 분이 예매 중인 좌석입니다. id=" + seatId));
        try {
            return noLockSeatReserver.reserve(seatId, userId);
        } finally {
            redisLock.unlock(key, token);
        }
    }
}

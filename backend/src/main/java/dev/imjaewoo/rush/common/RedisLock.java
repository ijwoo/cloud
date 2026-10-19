package dev.imjaewoo.rush.common;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SET key token NX PX 로 잡고, 내 토큰일 때만 지우는 단순한 분산 락.
 * 라이브러리(Redisson 등) 없이 직접 짜서 무엇이 필요한지 확인하기 위한 구현이다.
 */
@Component
public class RedisLock {

    /** 다른 사람의 락을 지우지 않도록 "값이 내 토큰이면 삭제"를 원자적으로 한다. */
    private static final RedisScript<Long> UNLOCK = RedisScript.of("""
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redis;

    public RedisLock(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 락을 잡으면 해제에 쓸 토큰을, 이미 누가 잡고 있으면 empty를 돌려준다. */
    public Optional<String> tryLock(String key, Duration leaseTime) {
        String token = UUID.randomUUID().toString();
        Boolean acquired = redis.opsForValue().setIfAbsent(key, token, leaseTime);
        return Boolean.TRUE.equals(acquired) ? Optional.of(token) : Optional.empty();
    }

    public void unlock(String key, String token) {
        redis.execute(UNLOCK, List.of(key), token);
    }
}

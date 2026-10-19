package dev.imjaewoo.rush.queue;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * 공연별 대기열. Redis 키 구성:
 * <pre>
 * queue:open                  대기열이 켜진 공연 id (SET)
 * queue:{id}:seq              입장 순번 발급용 카운터
 * queue:{id}:waiting          token → 순번 (ZSET, 순번이 작을수록 앞)
 * queue:{id}:admitted:{token} 입장 허가 (TTL이 지나면 사라짐)
 * </pre>
 */
@Component
public class WaitingQueue {

    private final StringRedisTemplate redis;
    private final QueueProperties properties;

    public WaitingQueue(StringRedisTemplate redis, QueueProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    public void open(Long concertId) {
        redis.opsForSet().add("queue:open", concertId.toString());
    }

    public void close(Long concertId) {
        redis.opsForSet().remove("queue:open", concertId.toString());
        redis.delete(List.of(waitingKey(concertId), seqKey(concertId)));
    }

    public boolean isOpen(Long concertId) {
        return Boolean.TRUE.equals(redis.opsForSet().isMember("queue:open", concertId.toString()));
    }

    public Set<Long> openConcerts() {
        Set<String> members = redis.opsForSet().members("queue:open");
        Set<Long> ids = new HashSet<>();
        if (members != null) {
            members.forEach(m -> ids.add(Long.valueOf(m)));
        }
        return ids;
    }

    public String enter(Long concertId) {
        String token = UUID.randomUUID().toString();
        enter(concertId, token);
        return token;
    }

    /** 실험용 가상 대기자를 한 번에 줄 세운다. */
    public void enterBots(Long concertId, int count) {
        Long last = redis.opsForValue().increment(seqKey(concertId), count);
        long first = last - count + 1;
        Set<TypedTuple<String>> bots = new HashSet<>();
        IntStream.range(0, count).forEach(i ->
                bots.add(TypedTuple.of("bot-" + (first + i), (double) (first + i))));
        redis.opsForZSet().add(waitingKey(concertId), bots);
    }

    public QueueStatus status(Long concertId, String token) {
        if (isAdmitted(concertId, token)) {
            return new QueueStatus(QueueStatus.State.ADMITTED, 0, waitingCount(concertId), properties.admitPerSecond());
        }
        Long rank = redis.opsForZSet().rank(waitingKey(concertId), token);
        if (rank == null) {
            return new QueueStatus(QueueStatus.State.EXPIRED, 0, waitingCount(concertId), properties.admitPerSecond());
        }
        return new QueueStatus(QueueStatus.State.WAITING, rank, waitingCount(concertId), properties.admitPerSecond());
    }

    public boolean isAdmitted(Long concertId, String token) {
        return token != null && Boolean.TRUE.equals(redis.hasKey(admittedKey(concertId, token)));
    }

    /** 맨 앞에서 n명을 꺼내 입장 허가를 준다. 가상 대기자는 꺼내기만 한다. */
    public int admit(Long concertId, int n) {
        Set<TypedTuple<String>> popped = redis.opsForZSet().popMin(waitingKey(concertId), n);
        if (popped == null) {
            return 0;
        }
        popped.stream()
                .map(TypedTuple::getValue)
                .filter(token -> token != null && !token.startsWith("bot-"))
                .forEach(token -> redis.opsForValue().set(admittedKey(concertId, token), "1", properties.admittedTtl()));
        return popped.size();
    }

    public long waitingCount(Long concertId) {
        Long size = redis.opsForZSet().zCard(waitingKey(concertId));
        return size == null ? 0 : size;
    }

    private void enter(Long concertId, String token) {
        Long seq = redis.opsForValue().increment(seqKey(concertId));
        redis.opsForZSet().add(waitingKey(concertId), token, seq);
    }

    private static String waitingKey(Long concertId) {
        return "queue:" + concertId + ":waiting";
    }

    private static String seqKey(Long concertId) {
        return "queue:" + concertId + ":seq";
    }

    private static String admittedKey(Long concertId, String token) {
        return "queue:" + concertId + ":admitted:" + token;
    }
}

package dev.imjaewoo.rush.queue;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 1초마다 대기열이 켜진 공연마다 정해진 인원을 입장시킨다. */
@Component
@ConditionalOnProperty(name = "rush.queue.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class QueueAdmitter {

    private final WaitingQueue waitingQueue;
    private final QueueProperties properties;

    public QueueAdmitter(WaitingQueue waitingQueue, QueueProperties properties) {
        this.waitingQueue = waitingQueue;
        this.properties = properties;
    }

    @Scheduled(fixedRate = 1000)
    public void admit() {
        waitingQueue.openConcerts().forEach(id -> waitingQueue.admit(id, properties.admitPerSecond()));
    }
}

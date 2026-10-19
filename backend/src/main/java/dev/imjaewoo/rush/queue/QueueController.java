package dev.imjaewoo.rush.queue;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class QueueController {

    private final WaitingQueue waitingQueue;

    public QueueController(WaitingQueue waitingQueue) {
        this.waitingQueue = waitingQueue;
    }

    @GetMapping("/api/queue/{concertId}")
    public Map<String, Object> info(@PathVariable Long concertId) {
        return Map.of("open", waitingQueue.isOpen(concertId), "waiting", waitingQueue.waitingCount(concertId));
    }

    @PostMapping("/api/queue/{concertId}")
    public Map<String, String> enter(@PathVariable Long concertId) {
        return Map.of("token", waitingQueue.enter(concertId));
    }

    @GetMapping("/api/queue/{concertId}/{token}")
    public QueueStatus status(@PathVariable Long concertId, @PathVariable String token) {
        return waitingQueue.status(concertId, token);
    }

    /** 대기열을 켜고, 실험용 가상 대기자를 bots명 줄 세운다. */
    @PostMapping("/api/admin/queue/{concertId}/open")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void open(@PathVariable Long concertId, @RequestParam(defaultValue = "0") int bots) {
        waitingQueue.open(concertId);
        if (bots > 0) {
            waitingQueue.enterBots(concertId, Math.min(bots, 100_000));
        }
    }

    @PostMapping("/api/admin/queue/{concertId}/close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void close(@PathVariable Long concertId) {
        waitingQueue.close(concertId);
    }
}

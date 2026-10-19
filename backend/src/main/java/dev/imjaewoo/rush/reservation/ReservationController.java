package dev.imjaewoo.rush.reservation;

import dev.imjaewoo.rush.common.QueueRequiredException;
import dev.imjaewoo.rush.concert.SeatRepository;
import dev.imjaewoo.rush.queue.WaitingQueue;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final SeatRepository seatRepository;
    private final WaitingQueue waitingQueue;

    public ReservationController(ReservationService reservationService,
                                 SeatRepository seatRepository,
                                 WaitingQueue waitingQueue) {
        this.reservationService = reservationService;
        this.seatRepository = seatRepository;
        this.waitingQueue = waitingQueue;
    }

    /** 대기열이 켜진 공연이면 입장 허가 토큰(X-Queue-Token)이 있어야 예매할 수 있다. */
    @PostMapping("/api/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(@Valid @RequestBody ReservationRequest request,
                                       @RequestParam(defaultValue = "NONE") LockStrategy strategy,
                                       @RequestHeader(name = "X-Queue-Token", required = false) String queueToken) {
        Long concertId = seatRepository.findConcertIdBySeatId(request.seatId());
        if (concertId != null && waitingQueue.isOpen(concertId) && !waitingQueue.isAdmitted(concertId, queueToken)) {
            throw new QueueRequiredException("대기열을 거쳐 입장한 뒤 예매할 수 있습니다.");
        }
        return ReservationResponse.from(reservationService.reserve(request.seatId(), request.userId(), strategy));
    }

    @GetMapping("/api/admin/concerts/{concertId}/stats")
    public ReservationStats stats(@PathVariable Long concertId) {
        return reservationService.stats(concertId);
    }

    @PostMapping("/api/admin/concerts/{concertId}/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@PathVariable Long concertId) {
        reservationService.reset(concertId);
    }
}

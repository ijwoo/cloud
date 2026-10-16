package dev.imjaewoo.rush.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/api/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(@Valid @RequestBody ReservationRequest request,
                                       @RequestParam(defaultValue = "NONE") LockStrategy strategy) {
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

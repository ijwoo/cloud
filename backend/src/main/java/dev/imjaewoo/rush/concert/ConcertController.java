package dev.imjaewoo.rush.concert;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/concerts")
public class ConcertController {

    private final ConcertService concertService;

    public ConcertController(ConcertService concertService) {
        this.concertService = concertService;
    }

    @GetMapping
    public List<ConcertResponse> findAll() {
        return concertService.findAll();
    }

    @GetMapping("/{concertId}")
    public ConcertResponse findById(@PathVariable Long concertId) {
        return concertService.findById(concertId);
    }

    @GetMapping("/{concertId}/seats")
    public List<SeatResponse> findSeats(@PathVariable Long concertId) {
        return concertService.findSeats(concertId);
    }
}

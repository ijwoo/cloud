package dev.imjaewoo.rush.concert;

import dev.imjaewoo.rush.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final SeatRepository seatRepository;

    public ConcertService(ConcertRepository concertRepository, SeatRepository seatRepository) {
        this.concertRepository = concertRepository;
        this.seatRepository = seatRepository;
    }

    public List<ConcertResponse> findAll() {
        return concertRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ConcertResponse findById(Long concertId) {
        return toResponse(getConcert(concertId));
    }

    public List<SeatResponse> findSeats(Long concertId) {
        getConcert(concertId);
        return seatRepository.findByConcertIdOrderByIdAsc(concertId).stream()
                .map(SeatResponse::from)
                .toList();
    }

    private Concert getConcert(Long concertId) {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new NotFoundException("공연을 찾을 수 없습니다. id=" + concertId));
    }

    private ConcertResponse toResponse(Concert concert) {
        long available = seatRepository.countByConcertIdAndStatus(concert.getId(), SeatStatus.AVAILABLE);
        long reserved = seatRepository.countByConcertIdAndStatus(concert.getId(), SeatStatus.RESERVED);
        return new ConcertResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getArtist(),
                concert.getVenue(),
                concert.getStartsAt(),
                available + reserved,
                available
        );
    }
}

package dev.imjaewoo.rush.concert;

import java.time.OffsetDateTime;

public record ConcertResponse(
        Long id,
        String title,
        String artist,
        String venue,
        OffsetDateTime startsAt,
        long totalSeats,
        long availableSeats
) {
}

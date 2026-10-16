package dev.imjaewoo.rush.concert;

public record SeatResponse(
        Long id,
        String section,
        String rowLabel,
        int number,
        int price,
        SeatStatus status
) {

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getSection(),
                seat.getRowLabel(),
                seat.getNumber(),
                seat.getPrice(),
                seat.getStatus()
        );
    }
}

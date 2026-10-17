package dev.imjaewoo.rush.reservation;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class SeatClaimRepository {

    private final JdbcClient jdbcClient;

    public SeatClaimRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * 선점에 성공하면 true. 같은 좌석을 먼저 insert 한 트랜잭션이 있으면
     * 그 트랜잭션이 끝날 때까지 기다렸다가, 커밋됐으면 false를 돌려준다.
     */
    public boolean claim(Long seatId, String userId) {
        return jdbcClient.sql("""
                        insert into seat_claim (seat_id, user_id) values (:seatId, :userId)
                        on conflict (seat_id) do nothing
                        """)
                .param("seatId", seatId)
                .param("userId", userId)
                .update() == 1;
    }

    public void deleteBySeatId(Long seatId) {
        jdbcClient.sql("delete from seat_claim where seat_id = :seatId").param("seatId", seatId).update();
    }

    public void deleteByConcertId(Long concertId) {
        jdbcClient.sql("""
                        delete from seat_claim
                        where seat_id in (select id from seat where concert_id = :concertId)
                        """)
                .param("concertId", concertId)
                .update();
    }
}

package dev.imjaewoo.rush.queue;

import dev.imjaewoo.rush.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "rush.queue.scheduler-enabled=false")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class WaitingQueueTest {

    private static final long CONCERT_ID = 1L;

    @Autowired
    WaitingQueue waitingQueue;

    @Autowired
    MockMvc mockMvc;

    @AfterEach
    void tearDown() {
        waitingQueue.close(CONCERT_ID);
    }

    @Test
    void 먼저_줄_선_순서대로_입장한다() {
        waitingQueue.open(CONCERT_ID);
        waitingQueue.enterBots(CONCERT_ID, 120);
        String me = waitingQueue.enter(CONCERT_ID);

        assertThat(waitingQueue.status(CONCERT_ID, me).ahead()).isEqualTo(120);

        waitingQueue.admit(CONCERT_ID, 100);
        QueueStatus afterFirst = waitingQueue.status(CONCERT_ID, me);
        assertThat(afterFirst.state()).isEqualTo(QueueStatus.State.WAITING);
        assertThat(afterFirst.ahead()).isEqualTo(20);

        waitingQueue.admit(CONCERT_ID, 50);
        assertThat(waitingQueue.status(CONCERT_ID, me).state()).isEqualTo(QueueStatus.State.ADMITTED);
        assertThat(waitingQueue.isAdmitted(CONCERT_ID, me)).isTrue();
    }

    @Test
    void 대기열이_켜진_공연은_입장_토큰_없이_예매할_수_없다() throws Exception {
        waitingQueue.open(CONCERT_ID);
        String body = """
                {"seatId": 3, "userId": "no-token"}
                """;

        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        String token = waitingQueue.enter(CONCERT_ID);
        waitingQueue.admit(CONCERT_ID, 1);
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("X-Queue-Token", token))
                .andExpect(status().isCreated());
    }
}

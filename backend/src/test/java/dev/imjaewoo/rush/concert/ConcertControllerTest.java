package dev.imjaewoo.rush.concert;

import dev.imjaewoo.rush.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ConcertControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void 시드_공연과_좌석을_조회한다() throws Exception {
        mockMvc.perform(get("/api/concerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("RUSH LIVE 2026"))
                .andExpect(jsonPath("$[0].totalSeats").value(200))
                .andExpect(jsonPath("$[1].title").value("RUSH ARENA"))
                .andExpect(jsonPath("$[1].totalSeats").value(5000));

        mockMvc.perform(get("/api/concerts/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(200)));
    }

    @Test
    void 없는_공연은_404() throws Exception {
        mockMvc.perform(get("/api/concerts/999"))
                .andExpect(status().isNotFound());
    }
}

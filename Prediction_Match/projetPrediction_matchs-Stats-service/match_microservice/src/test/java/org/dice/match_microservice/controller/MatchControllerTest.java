package org.dice.match_microservice.controller;

import org.dice.match_microservice.entity.Match;
import org.dice.match_microservice.entity.MatchStatus;
import org.dice.match_microservice.repository.matchRepository;
import org.dice.match_microservice.service.MatchDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.StatusAggregator;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(MatchController.class)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MatchDataService matchDataService;
    @MockBean
    private matchRepository matchRepository;

    @WithMockUser
    @Test
    void fetchMatchesForThisWeek() throws Exception {
        Match match = new Match();
        match.setHomeTeamName("Arsenal");
        match.setStatus(MatchStatus.SCHEDULED);

        given(matchRepository.findByMatchDateBetweenAndStatus(any(),any(), eq(MatchStatus.SCHEDULED)))
                .willReturn(List.of(match));

        mockMvc.perform(get("/api/matches/upcoming")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].homeTeamName").value("Arsenal"));

    }

    @WithMockUser
    @Test
    void syncMatches() throws Exception {
        mockMvc.perform(post("/api/matches/sync") .with(csrf()))

                .andExpect(status().isOk())
                .andExpect(content().string("Sync triggered successfully"));

        verify(matchDataService).fetchCalendar();

    }
}
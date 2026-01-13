package org.dice.match_microservice.service;

import org.dice.match_microservice.entity.Match;
import org.dice.match_microservice.entity.MatchStatus;
import org.dice.match_microservice.messaging.MatchEventProducer;
import org.dice.match_microservice.repository.matchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchDataServiceTest {
        @Mock
        private matchRepository matchRepository;
        @Mock
        private MatchEventProducer matchEventProducer;
        @InjectMocks
        private MatchDataService matchDataService;
    @Test
    void triggerLastFinishedMatch() {

        Match mockMatch = new Match();
        mockMatch.setId(1L);
        mockMatch.setHomeTeamName("Team A");
        mockMatch.setAwayTeamName("Team B");
        when(matchRepository.findTopByStatusOrderByMatchDateDesc(MatchStatus.FINISHED))
                .thenReturn(Optional.of(mockMatch));
        matchDataService.triggerLastFinishedMatch();
        verify(matchEventProducer, times(1)).sendMatchFinishedEvent(mockMatch);
    }
}
package org.dice.match_microservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.dice.match_microservice.dto.MatchFinishedEvent;
import org.dice.match_microservice.entity.Match;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper; // Spring will inject this
    private static final String matchEventsTopic = "match-events";

    public void sendMatchFinishedEvent(Match match) {
        try {
            System.out.println("-> Preparing event for match: " + match.getMatchId());

            MatchFinishedEvent event = new MatchFinishedEvent(
                    match.getId(),
                    match.getHomeTeamName(),
                    match.getAwayTeamName(),
                    match.getMatchDate().toLocalDate().toString()
            );

            String jsonMessage = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(matchEventsTopic, jsonMessage);
            System.out.println("-> Sent JSON Event: " + jsonMessage);

        } catch (Exception e) {
            System.err.println("Error sending match event: " + e.getMessage());
        }
    }
}
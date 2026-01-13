//package org.example.statsservice.messaging;
//
//import org.example.statsservice.service.StatsService;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//import org.springframework.beans.factory.annotation.Autowired;
//
//@Component
//public class MatchEventListener {
//
//    @Autowired
//    private StatsService statsService;
//
//    // Écoute le topic où le Match-Service parle
//    @KafkaListener(topics = "match-events", groupId = "stats-group")
//    public void handleMatchFinished(String message) {
//        // Supposons que le message soit juste l'ID pour simplifier : "12345"
//        // En vrai, tu utiliseras un parser JSON (Jackson) pour lire l'objet
//        System.out.println("Match terminé reçu : " + message);
//
//        Long matchId = Long.parseLong(message); // Extraction basique
//        statsService.fetchAndSaveStats(matchId);
//    }
//}

package org.example.statsservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.example.statsservice.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MatchEventListener {

    @Autowired
    private StatsService statsService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "match-events", groupId = "stats-group")
    public void handleMatchFinished(String message) {
        try {
            EventDto event = objectMapper.readValue(message, EventDto.class);

            System.out.println("Event Received: " + event.getHomeTeam() + " vs " + event.getAwayTeam());

            statsService.processMatchEvent(
                    event.getMatchId(),
                    event.getHomeTeam(),
                    event.getAwayTeam(),
                    event.getMatchDate()
            );

        } catch (Exception e) {
            System.err.println("Error parsing match event: " + e.getMessage());
        }
    }

    @Data
    static class EventDto {
        private Long matchId;
        private String homeTeam;
        private String awayTeam;
        private String matchDate;
    }
}